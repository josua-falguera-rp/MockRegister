// src/RegisterController.java

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;

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
            existingItem.addQuantity(qty);
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice());
        } else {
            TransactionItem newItem = new TransactionItem(product, qty);
            currentTransaction.add(newItem);
            journal.logItem(product.getUpc(), product.getName(),
                    product.getPrice());
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
                    item.getProduct().getPrice());

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
        if (currentDiscount.isSuccessful() &&
                currentDiscount.getStatus() == DiscountService.DiscountResult.Status.FALLBACK) {
            System.out.println("Discount API fallback: " + currentDiscount.getMessage());
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
                // Create styled confirmation panel
                JPanel confirmPanel = new JPanel(new BorderLayout(10, 15));
                confirmPanel.setBackground(Color.WHITE);
                confirmPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

                JLabel titleLabel = new JLabel("⚠️ Current Transaction Active", SwingConstants.CENTER);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
                titleLabel.setForeground(new Color(251, 146, 60)); // ACCENT_ORANGE

                JPanel infoPanel = getJPanel();

                confirmPanel.add(titleLabel, BorderLayout.NORTH);
                confirmPanel.add(infoPanel, BorderLayout.CENTER);

                int confirm = JOptionPane.showConfirmDialog(
                        ui,
                        confirmPanel,
                        "Save Current Transaction?",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    suspendTransaction();
                } else if (confirm == JOptionPane.CANCEL_OPTION || confirm == JOptionPane.CLOSED_OPTION) {
                    return;
                }
            }

            // Create custom selection dialog
            JPanel selectionPanel = new JPanel(new BorderLayout(10, 15));
            selectionPanel.setBackground(Color.WHITE);
            selectionPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel titleLabel = new JLabel("▶️ Resume Transaction", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLabel.setForeground(new Color(251, 146, 60)); // ACCENT_ORANGE

            JLabel instructionLabel = new JLabel("Select a suspended transaction to resume:");
            instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            instructionLabel.setForeground(new Color(107, 114, 128)); // TEXT_SECONDARY
            instructionLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

            // Create list of transactions with details
            DefaultListModel<String> listModel = new DefaultListModel<>();
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");

            for (Integer id : suspendedIds) {
                try {
                    // Get transaction details for better display
                    Map<String, Object> transData = dbManager.getTransactionById(id);
                    if (transData != null) {
                        double total = (Double) transData.get("total");
                        java.util.Date date = (java.util.Date) transData.get("date");

                        listModel.addElement(String.format("Transaction #%d - $%s - %s",
                                id, df.format(total), dateFormat.format(date)));
                    } else {
                        listModel.addElement("Transaction #" + id);
                    }
                } catch (Exception e) {
                    listModel.addElement("Transaction #" + id);
                }
            }

            JList<String> transactionList = new JList<>(listModel);
            transactionList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            transactionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            transactionList.setSelectedIndex(0);
            transactionList.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true));
            transactionList.setFixedCellHeight(40);

            JScrollPane scrollPane = new JScrollPane(transactionList);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true));

            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.add(instructionLabel, BorderLayout.NORTH);
            contentPanel.add(scrollPane, BorderLayout.CENTER);

            selectionPanel.add(titleLabel, BorderLayout.NORTH);
            selectionPanel.add(contentPanel, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(
                    ui,
                    selectionPanel,
                    "Resume Transaction",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION && transactionList.getSelectedValue() != null) {
                String selected = transactionList.getSelectedValue();
                // Extract transaction ID from the formatted string
                int transactionId = Integer.parseInt(selected.replaceAll("Transaction #(\\d+).*", "$1"));

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
            }
        } catch (SQLException e) {
            ui.showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            ui.showError("Invalid transaction selection");
        }
    }

    private static JPanel getJPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel info1 = new JLabel("You have items in the current transaction.");
        info1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info1.setForeground(new Color(17, 24, 39));

        JLabel info2 = new JLabel("Would you like to suspend it before resuming another?");
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info2.setForeground(new Color(17, 24, 39));

        infoPanel.add(info1);
        infoPanel.add(info2);
        return infoPanel;
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

            // NOTE: Receipt display is now handled by PaymentPanel
            // No longer showing payment complete dialog here

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

    /**
     * Returns the current transaction ID.
     * Returns -1 if no transaction is active.
     */
    public int getCurrentTransactionId() {
        return currentTransactionId;
    }
}