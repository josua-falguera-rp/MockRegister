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

    public void addItem(String upc, int qty) {
        try {
            Product product = dbManager.getProductByUPC(upc);
            if (product == null) {
                ui.showError("Product not found with UPC: " + upc);
                return;
            }

            // Create transaction if this is the first item
            if (currentTransactionId == -1 && currentTransaction.isEmpty()) {
                currentTransactionId = saveInitialTransaction();
                journal.logTransactionStart(currentTransactionId);
            }

            addOrUpdateTransactionItem(product, qty);

            // Update database totals and items after adding
            updateTransactionInDatabase();

            refreshUI();
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    private void addOrUpdateTransactionItem(Product product, int qty) {
        TransactionItem existingItem = findItemByUPC(product.getUpc());

        if (existingItem != null) {
            // Item already exists, update quantity
            int oldQty = existingItem.getQuantity();
            existingItem.addQuantity(qty);

            // Log to journal
            journal.logQuantityChange(product.getUpc(), product.getName(),
                    oldQty, existingItem.getQuantity());
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice(), existingItem.getQuantity(), existingItem.getTotal());
        } else {
            // New item
            TransactionItem newItem = new TransactionItem(product, qty);
            currentTransaction.add(newItem);

            // Log to journal
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice(), qty, newItem.getTotal());
        }
    }

    public void voidItem(int index) {
        if (index >= 0 && index < currentTransaction.size()) {
            TransactionItem item = currentTransaction.get(index);

            // Log to journal
            journal.logVoidItem(item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    item.getQuantity());

            // Remove from the current transaction
            currentTransaction.remove(index);

            // Update database
            try {
                if (currentTransactionId != -1) {
                    updateTransactionInDatabase();
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

            // Log to journal
            journal.logQuantityChange(item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    oldQty, newQty);
            journal.logItem(item.getProduct().getUpc(), item.getProduct().getName(),
                    item.getProduct().getPrice(), newQty, item.getTotal());

            // Update database
            try {
                if (currentTransactionId != -1) {
                    updateTransactionInDatabase();
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
                    currentTransactionId = saveInitialTransaction();
                }

                // Log to journal
                journal.logVoidTransaction(currentTransactionId);

                // Void in database
                dbManager.voidTransaction(currentTransactionId, "Voided by cashier");

                ui.showMessage("Transaction #" + currentTransactionId + " has been voided");

                // Clear current transaction and start fresh
                currentTransaction.clear();
                currentTransactionId = -1;
                isResumedTransaction = false;
                refreshUI();
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
                    currentTransactionId = saveInitialTransaction();
                    journal.logTransactionStart(currentTransactionId);
                }

                // Make sure all items and totals are saved
                saveAllTransactionItems();
                updateTransactionInDatabase();

                // Log to journal
                journal.logSuspendTransaction(currentTransactionId);

                // Suspend in database
                dbManager.suspendTransaction(currentTransactionId);

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

                // Log to journal
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

            // Ensure transaction is saved with all items
            if (currentTransactionId == -1) {
                currentTransactionId = saveInitialTransaction();
                journal.logTransactionStart(currentTransactionId);
            }

            // Save or update all items one final time
            saveAllTransactionItems();
            updateTransactionInDatabase();

            // Log to journal
            journal.logSubtotal(subtotal);
            journal.logTax(tax);
            journal.logTotal(total);
            journal.logPayment(paymentType, tendered, change);
            journal.logTransactionComplete(currentTransactionId);

            // Update payment information in database
            dbManager.updateTransactionPayment(currentTransactionId, paymentType, tendered, change);

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

    private int saveInitialTransaction() throws SQLException {
        double subtotal = getSubtotal();
        double tax = getTax();
        double total = getTotal();

        return dbManager.saveTransaction(subtotal, tax, total);
    }

    private void saveAllTransactionItems() throws SQLException {
        if (currentTransactionId != -1) {
            // Clear existing non-voided items for this transaction to avoid duplicates
            dbManager.clearTransactionItems(currentTransactionId);

            // Save all current items
            for (TransactionItem item : currentTransaction) {
                dbManager.saveTransactionItem(currentTransactionId, item);
            }
        }
    }

    private void updateTransactionInDatabase() throws SQLException {
        if (currentTransactionId != -1) {
            // First save all items (clearing old ones to avoid duplicates)
            saveAllTransactionItems();

            // Then update transaction totals
            double subtotal = getSubtotal();
            double tax = getTax();
            double total = getTotal();
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
}