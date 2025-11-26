import java.sql.SQLException;
import java.util.*;

public class RegisterController {
    private static final double TAX_RATE = 0.07;

    private final DatabaseManager dbManager;
    private final VirtualJournal journal;
    private final DiscountService discountService;
    private final List<TransactionItem> currentTransaction;
    private RegisterUI ui;
    private int currentTransactionId = -1;
    private boolean isResumedTransaction = false;

    // Cached discount result for current transaction
    private DiscountService.DiscountResult currentDiscount = null;

    public RegisterController(DatabaseManager dbManager, VirtualJournal journal) {
        this.dbManager = dbManager;
        this.journal = journal;
        this.currentTransaction = new ArrayList<>();

        // Initialize discount service with default config
        ApiConfig apiConfig = new ApiConfig();
        this.discountService = new DiscountService(apiConfig);
    }

    /**
     * Alternative constructor with custom API configuration.
     */
    public RegisterController(DatabaseManager dbManager, VirtualJournal journal, ApiConfig apiConfig) {
        this.dbManager = dbManager;
        this.journal = journal;
        this.currentTransaction = new ArrayList<>();
        this.discountService = new DiscountService(apiConfig);
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

            if (currentTransactionId == -1 && currentTransaction.isEmpty()) {
                currentTransactionId = saveInitialTransaction();
                journal.logTransactionStart(currentTransactionId);
            }

            addOrUpdateTransactionItem(product, qty);
            updateTransactionInDatabase();

            // Recalculate discounts when items change
            recalculateDiscount();

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
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice(), existingItem.getQuantity(), existingItem.getTotal());
        } else {
            TransactionItem newItem = new TransactionItem(product, qty);
            currentTransaction.add(newItem);
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice(), qty, newItem.getTotal());
        }
    }

    public void voidItem(int index) {
        if (index >= 0 && index < currentTransaction.size()) {
            TransactionItem item = currentTransaction.get(index);
            journal.logVoidItem(item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    item.getQuantity());

            currentTransaction.remove(index);

            try {
                if (currentTransactionId != -1) {
                    updateTransactionInDatabase();
                }
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }

            // Recalculate discounts when items change
            recalculateDiscount();

            refreshUI();
        }
    }

    public void changeQuantity(int index, int newQty) {
        if (index >= 0 && index < currentTransaction.size() && newQty > 0) {
            TransactionItem item = currentTransaction.get(index);
            int oldQty = item.getQuantity();
            item.setQuantity(newQty);

            journal.logQuantityChange(item.getProduct().getUpc(),
                    item.getProduct().getName(), oldQty, newQty);
            journal.logItem(item.getProduct().getUpc(), item.getProduct().getName(),
                    item.getProduct().getPrice(), newQty, item.getTotal());

            try {
                if (currentTransactionId != -1) {
                    updateTransactionInDatabase();
                }
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }

            // Recalculate discounts when quantity changes
            recalculateDiscount();

            refreshUI();
        }
    }

    /**
     * Recalculates discounts for the current transaction.
     */
    private void recalculateDiscount() {
        if (currentTransaction.isEmpty()) {
            currentDiscount = null;
            return;
        }

        currentDiscount = discountService.calculateDiscount(currentTransaction);

        // Log discount status if there's an issue
        if (!currentDiscount.isSuccessful() &&
                currentDiscount.getStatus() == DiscountService.DiscountResult.Status.FALLBACK) {
            System.out.println("Discount API fallback: " + currentDiscount.getMessage());
        }
    }

    /**
     * Manually triggers discount recalculation (can be called from UI).
     */
    public void applyDiscounts() {
        recalculateDiscount();
        refreshUI();

        if (currentDiscount != null && currentDiscount.hasDiscount()) {
            ui.showMessage("Discounts applied: " +
                    String.join(", ", currentDiscount.getAppliedDiscounts()));
        } else if (currentDiscount != null && !currentDiscount.isSuccessful()) {
            ui.showMessage("Could not apply discounts: " + currentDiscount.getMessage());
        } else {
            ui.showMessage("No discounts available for current items");
        }
    }

    public void voidTransaction() {
        if (!currentTransaction.isEmpty() || currentTransactionId != -1) {
            try {
                if (currentTransactionId == -1) {
                    currentTransactionId = saveInitialTransaction();
                }

                journal.logVoidTransaction(currentTransactionId);
                dbManager.voidTransaction(currentTransactionId, "Voided by cashier");
                ui.showMessage("Transaction #" + currentTransactionId + " has been voided");

                clearCurrentTransaction();
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
                if (currentTransactionId == -1) {
                    currentTransactionId = saveInitialTransaction();
                    journal.logTransactionStart(currentTransactionId);
                }

                saveAllTransactionItems();
                updateTransactionInDatabase();
                journal.logSuspendTransaction(currentTransactionId);
                dbManager.suspendTransaction(currentTransactionId);

                ui.showMessage("Transaction #" + currentTransactionId + " has been suspended");

                clearCurrentTransaction();
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

            if (!currentTransaction.isEmpty()) {
                int confirm = ui.confirmDialog("Save current transaction before resuming?",
                        "Current Transaction");
                if (confirm == 0) {
                    suspendTransaction();
                } else if (confirm == 2) {
                    return;
                }
            }

            String[] options = suspendedIds.stream()
                    .map(id -> "Transaction #" + id)
                    .toArray(String[]::new);

            String selected = ui.showSelectionDialog("Select transaction to resume:",
                    "Resume Transaction", options);

            if (selected != null) {
                int transactionId = Integer.parseInt(selected.replace("Transaction #", ""));
                Map<String, Object> transData = dbManager.resumeTransaction(transactionId);

                currentTransaction.clear();
                @SuppressWarnings("unchecked")
                List<TransactionItem> items = (List<TransactionItem>) transData.get("items");
                currentTransaction.addAll(items);

                currentTransactionId = transactionId;
                isResumedTransaction = true;

                journal.logResumeTransaction(transactionId);

                // Recalculate discounts for resumed transaction
                recalculateDiscount();

                refreshUI();
                ui.showMessage("Transaction #" + transactionId + " resumed");
            }
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            ui.showError("Invalid transaction selection");
        }
    }

    /**
     * Completes the transaction with the specified payment.
     * Called from PaymentPanel.
     */
    public void completeTransaction(String paymentType, double tendered) {
        if (currentTransaction.isEmpty()) {
            ui.showError("No items in transaction");
            return;
        }

        try {
            // Ensure discounts are calculated
            if (currentDiscount == null) {
                recalculateDiscount();
            }

            double subtotal = getSubtotal();
            double discountAmount = getDiscountAmount();
            double discountedSubtotal = subtotal - discountAmount;
            double tax = discountedSubtotal * TAX_RATE;
            double total = discountedSubtotal + tax;
            double change = tendered - total;

            if (change < 0) {
                ui.showError("Insufficient payment");
                return;
            }

            if (currentTransactionId == -1) {
                currentTransactionId = saveInitialTransaction();
                journal.logTransactionStart(currentTransactionId);
            }

            saveAllTransactionItems();
            updateTransactionInDatabase();

            // Log to journal
            journal.logSubtotal(subtotal);
            if (discountAmount > 0) {
                journal.logDiscount(discountAmount, getAppliedDiscounts());
            }
            journal.logTax(tax);
            journal.logTotal(total);
            journal.logPayment(paymentType, tendered, change);
            journal.logTransactionComplete(currentTransactionId);

            dbManager.updateTransactionPayment(currentTransactionId, paymentType, tendered, change);

            // Show payment complete dialog
            ui.showPaymentComplete(subtotal, discountAmount, tax, total, tendered, change);

            clearCurrentTransaction();
            refreshUI();
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    /**
     * Clears current transaction state.
     */
    private void clearCurrentTransaction() {
        currentTransaction.clear();
        currentTransactionId = -1;
        isResumedTransaction = false;
        currentDiscount = null;
    }

    private int saveInitialTransaction() throws SQLException {
        double subtotal = getSubtotal();
        double tax = getTax();
        double total = getTotal();
        return dbManager.saveTransaction(subtotal, tax, total);
    }

    private void saveAllTransactionItems() throws SQLException {
        if (currentTransactionId != -1) {
            dbManager.clearTransactionItems(currentTransactionId);
            for (TransactionItem item : currentTransaction) {
                dbManager.saveTransactionItem(currentTransactionId, item);
            }
        }
    }

    private void updateTransactionInDatabase() throws SQLException {
        if (currentTransactionId != -1) {
            saveAllTransactionItems();
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

        // Update totals with discount info
        ui.updateTotals(getSubtotal(), getDiscountAmount(), getTax(), getTotal());

        // Update discount status display
        if (currentDiscount != null && currentDiscount.hasDiscount()) {
            ui.setDiscountStatus(currentDiscount.getAppliedDiscounts());
        } else {
            ui.setDiscountStatus(null);
        }

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

    // ==================== Getter Methods ====================

    public double getSubtotal() {
        return currentTransaction.stream()
                .mapToDouble(TransactionItem::getTotal)
                .sum();
    }

    public double getDiscountAmount() {
        return currentDiscount != null ? currentDiscount.getDiscountAmount() : 0.0;
    }

    public List<String> getAppliedDiscounts() {
        return currentDiscount != null ? currentDiscount.getAppliedDiscounts() : List.of();
    }

    public double getTax() {
        double discountedSubtotal = getSubtotal() - getDiscountAmount();
        return discountedSubtotal * TAX_RATE;
    }

    public double getTotal() {
        double discountedSubtotal = getSubtotal() - getDiscountAmount();
        return discountedSubtotal + getTax();
    }

    public List<TransactionItem> getCurrentTransaction() {
        return new ArrayList<>(currentTransaction);
    }

    public DiscountService getDiscountService() {
        return discountService;
    }

    public boolean hasActiveDiscount() {
        return currentDiscount != null && currentDiscount.hasDiscount();
    }

    public int getCurrentTransactionId() {
        return currentTransactionId;
    }

    public boolean isResumedTransaction() {
        return isResumedTransaction;
    }
}