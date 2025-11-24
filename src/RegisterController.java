import java.sql.SQLException;
import java.util.*;

public class RegisterController {
    private static final double TAX_RATE = 0.07;

    private final DatabaseManager dbManager;
    private final VirtualJournal journal;
    private final List<TransactionItem> currentTransaction;
    private RegisterUI ui;
    private int currentTransactionId = -1;

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
            refreshUI();
        }
    }

    public void voidTransaction() {
        if (!currentTransaction.isEmpty() && currentTransactionId != -1) {
            try {
                dbManager.voidTransaction(currentTransactionId);
                journal.logVoidTransaction(currentTransactionId);
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }
        }
        currentTransaction.clear();
        currentTransactionId = -1;
        refreshUI();
    }

    public void suspendTransaction() {
        if (!currentTransaction.isEmpty()) {
            try {
                int transactionId = saveCurrentTransaction();
                dbManager.suspendTransaction(transactionId);
                journal.logSuspendTransaction(transactionId);
                currentTransaction.clear();
                currentTransactionId = -1;
                refreshUI();
            } catch (SQLException e) {
                ui.showError("Database error: " + e.getMessage());
            }
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

            int transactionId = saveCurrentTransaction();
            dbManager.updateTransactionPayment(transactionId, paymentType, tendered, change);

            journal.logSubtotal(subtotal);
            journal.logTax(tax);
            journal.logTotal(total);
            journal.logPayment(paymentType, tendered, change);
            journal.logTransactionComplete(transactionId);

            currentTransaction.clear();
            currentTransactionId = -1;
            refreshUI();

            ui.showPaymentComplete(total, tendered, change);
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        }
    }

    private int saveCurrentTransaction() throws SQLException {
        if (currentTransactionId == -1) {
            double subtotal = getSubtotal();
            double tax = getTax();
            double total = getTotal();

            currentTransactionId = dbManager.saveTransaction(subtotal, tax, total);
            journal.logTransactionStart(currentTransactionId);

            for (TransactionItem item : currentTransaction) {
                dbManager.saveTransactionItem(currentTransactionId, item);
            }
        }
        return currentTransactionId;
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