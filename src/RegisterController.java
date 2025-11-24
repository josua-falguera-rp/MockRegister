import java.sql.SQLException;
import java.util.*;

public class RegisterController {
    private static final double TAX_RATE = 0.07;

    private final DatabaseManager dbManager;
    private final VirtualJournal journal;
    private final List<TransactionItem> currentTransaction;
    private RegisterUI ui;
    private int currentTransactionId = -1;
    private boolean isResumedTransaction = false;

    public RegisterController(DatabaseManager dbManager, VirtualJournal journal) {
        this.dbManager = dbManager;
        this.journal = journal;
        this.currentTransaction = new ArrayList<>();
    }

    public void setUI(RegisterUI ui) {
        this.ui = ui;
    }

    public void startNewTransaction() {
        currentTransaction.clear();
        currentTransactionId = -1;
        isResumedTransaction = false;
        refreshUI();
    }

    public void addItem(String upc, int qty) {
        try {
            Product product = dbManager.getProductByUPC(upc);
            if (product == null) {
                ui.showError("Product not found with UPC: " + upc);
                return;
            }

            addOrUpdateTransactionItem(product, qty);

            // Save to database immediately if we have a transaction ID
            if (currentTransactionId == -1 && !currentTransaction.isEmpty()) {
                // Create the transaction in the database
                saveCurrentTransaction();
            }

            refreshUI();
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    private void addOrUpdateTransactionItem(Product product, int qty) {
        TransactionItem existingItem = findItemByUPC(product.getUpc());

        if (existingItem != null) {
            int oldQty = existingItem.getQuantity();
            existingItem.addQuantity(qty);
            journal.logQuantityChange(product.getUpc(), product.getName(),
                    oldQty, existingItem.getQuantity());
        } else {
            currentTransaction.add(new TransactionItem(product, qty));
        }

        journal.logItem(product.getUpc(), product.getName(),
                product.getPrice(), qty, product.getPrice() * qty);
    }

    public void voidItem(int index) {
        if (index >= 0 && index < currentTransaction.size()) {
            TransactionItem item = currentTransaction.get(index);
            journal.logVoidItem(item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    item.getQuantity());
            currentTransaction.remove(index);

            // Update database if transaction exists
            try {
                if (currentTransactionId != -1) {
                    updateTransactionTotals();
                }
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }

            refreshUI();
        }
    }

    public void changeQuantity(int index, int newQty) {
        if (index >= 0 && index < currentTransaction.size() && newQty > 0) {
            TransactionItem item = currentTransaction.get(index);
            int oldQty = item.getQuantity();
            item.setQuantity(newQty);
            journal.logQuantityChange(item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    oldQty, newQty);

            // Update database if transaction exists
            try {
                if (currentTransactionId != -1) {
                    updateTransactionTotals();
                }
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }

            refreshUI();
        }
    }

    public void voidTransaction() {
        if (!currentTransaction.isEmpty()) {
            try {
                // Ensure transaction is saved before voiding
                if (currentTransactionId == -1) {
                    saveCurrentTransaction();
                }

                // Void the transaction in database
                dbManager.voidTransaction(currentTransactionId, "Voided by cashier");
                journal.logVoidTransaction(currentTransactionId);

                // Clear current transaction and start fresh
                currentTransaction.clear();
                currentTransactionId = -1;
                isResumedTransaction = false;
                refreshUI();

                ui.showMessage("Transaction #" + currentTransactionId + " has been voided");
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }
        } else {
            ui.showError("No transaction to void");
        }
    }

    public void suspendTransaction() {
        if (!currentTransaction.isEmpty()) {
            try {
                // Save transaction if not already saved
                if (currentTransactionId == -1) {
                    saveCurrentTransaction();
                }

                // Suspend the transaction
                dbManager.suspendTransaction(currentTransactionId);
                journal.logSuspendTransaction(currentTransactionId);

                ui.showMessage("Transaction #" + currentTransactionId + " has been suspended");

                // Clear current transaction to start a new one
                currentTransaction.clear();
                currentTransactionId = -1;
                isResumedTransaction = false;
                refreshUI();
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }
        } else {
            ui.showError("No items to suspend");
        }
    }

    public void resumeTransaction() {
        try {
            List<Integer> suspendedIds = dbManager.getSuspendedTransactions();

            if (suspendedIds.isEmpty()) {
                ui.showError("No suspended transactions available");
                return;
            }

            // If there's a current transaction in progress, save it first
            if (!currentTransaction.isEmpty()) {
                int confirm = ui.confirmDialog("Save current transaction before resuming?",
                        "Current Transaction");
                if (confirm == 0) { // Yes
                    suspendTransaction();
                } else if (confirm == 2) { // Cancel
                    return;
                }
            }

            // Show list of suspended transactions
            String[] options = suspendedIds.stream()
                    .map(id -> "Transaction #" + id)
                    .toArray(String[]::new);

            String selected = ui.showSelectionDialog("Select transaction to resume:",
                    "Resume Transaction", options);

            if (selected != null) {
                int transactionId = Integer.parseInt(selected.replace("Transaction #", ""));

                // Resume the transaction
                Map<String, Object> transData = dbManager.resumeTransaction(transactionId);

                // Load the transaction items
                currentTransaction.clear();
                @SuppressWarnings("unchecked")
                List<TransactionItem> items = (List<TransactionItem>) transData.get("items");
                currentTransaction.addAll(items);

                currentTransactionId = transactionId;
                isResumedTransaction = true;

                journal.logResumeTransaction(transactionId);
                refreshUI();

                ui.showMessage("Transaction #" + transactionId + " resumed");
            }
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            ui.showError("Invalid transaction selection");
        }
    }

    public void completeTransaction(String paymentType, double tendered) {
        if (currentTransaction.isEmpty()) {
            ui.showError("No items in transaction");
            return;
        }

        try {
            double subtotal = getSubtotal();
            double tax = getTax();
            double total = getTotal();
            double change = tendered - total;

            if (change < 0) {
                ui.showError("Insufficient payment");
                return;
            }

            // Ensure transaction is saved
            if (currentTransactionId == -1) {
                saveCurrentTransaction();
            }

            // Update payment information
            dbManager.updateTransactionPayment(currentTransactionId, paymentType, tendered, change);

            journal.logSubtotal(subtotal);
            journal.logTax(tax);
            journal.logTotal(total);
            journal.logPayment(paymentType, tendered, change);
            journal.logTransactionComplete(currentTransactionId);

            // Clear for next transaction
            currentTransaction.clear();
            currentTransactionId = -1;
            isResumedTransaction = false;
            refreshUI();

            ui.showPaymentComplete(total, tendered, change);
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    private int saveCurrentTransaction() throws SQLException {
        if (currentTransactionId == -1 && !currentTransaction.isEmpty()) {
            double subtotal = getSubtotal();
            double tax = getTax();
            double total = getTotal();

            currentTransactionId = dbManager.saveTransaction(subtotal, tax, total);
            journal.logTransactionStart(currentTransactionId);

            // Save all items
            for (TransactionItem item : currentTransaction) {
                dbManager.saveTransactionItem(currentTransactionId, item);
            }
        }
        return currentTransactionId;
    }

    private void updateTransactionTotals() throws SQLException {
        if (currentTransactionId != -1) {
            // Recalculate totals
            double subtotal = getSubtotal();
            double tax = getTax();
            double total = getTotal();

            // Update transaction totals in database through DatabaseManager
            dbManager.updateTransactionTotals(currentTransactionId, subtotal, tax, total);
        }
    }

    public void showTransactionHistory() {
        try {
            List<Map<String, Object>> history = dbManager.getTransactionHistory(true, true);
            ui.showTransactionHistory(history);
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    private void refreshUI() {
        ui.clearTable();
        for (TransactionItem item : currentTransaction) {
            ui.addItemToTable(
                    item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    item.getProduct().getPrice(),
                    item.getQuantity(),
                    item.getTotal()
            );
        }
        ui.updateTotals(getSubtotal(), getTax(), getTotal());

        // Update UI to show transaction status
        if (currentTransactionId != -1) {
            String status = isResumedTransaction ? " (Resumed)" : "";
            ui.setTransactionStatus("Transaction #" + currentTransactionId + status);
        } else {
            ui.setTransactionStatus("");
        }
    }

    private TransactionItem findItemByUPC(String upc) {
        return currentTransaction.stream()
                .filter(item -> item.getProduct().getUpc().equals(upc))
                .findFirst()
                .orElse(null);
    }

    public double getSubtotal() {
        return currentTransaction.stream()
                .mapToDouble(TransactionItem::getTotal)
                .sum();
    }

    public double getTax() {
        return getSubtotal() * TAX_RATE;
    }

    public double getTotal() {
        return getSubtotal() + getTax();
    }

    public List<TransactionItem> getCurrentTransaction() {
        return new ArrayList<>(currentTransaction);
    }

    public List<Product> getQuickKeyProducts() {
        try {
            return dbManager.getQuickKeyProducts();
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}