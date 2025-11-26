import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Panel for building transactions - scanning items, adjusting quantities,
 * and managing the transaction before payment.
 */
public class TransactionPanel extends JPanel {

    // Color constants
    private static final Color BACKGROUND_GRAY = new Color(220, 220, 220);
    private static final Color BUTTON_GREEN = new Color(119, 214, 135);
    private static final Color BUTTON_RED = new Color(220, 100, 100);
    private static final Color BUTTON_BLUE = new Color(100, 150, 220);
    private static final Color BUTTON_YELLOW = new Color(255, 200, 100);
    private static final Color BUTTON_PURPLE = new Color(180, 130, 220);
    private static final Color DISCOUNT_GREEN = new Color(0, 150, 0);

    // Components
    private final RegisterController controller;
    private final Runnable onPaymentRequested;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // UI Elements
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel discountAmountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JLabel transactionStatusLabel;
    private JLabel discountStatusLabel;
    private JTextField upcInput;
    private JTextField qtyInput;
    private JTable itemTable;
    private JPanel quickKeysPanel;
    private JPanel discountPanel;
    private JButton payButton;

    public TransactionPanel(RegisterController controller, Runnable onPaymentRequested) {
        this.controller = controller;
        this.onPaymentRequested = onPaymentRequested;
        this.tableModel = createTableModel();

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // ==================== Top Panel ====================

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        transactionStatusLabel = new JLabel("");
        transactionStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        transactionStatusLabel.setForeground(new Color(0, 100, 0));

        discountStatusLabel = new JLabel("");
        discountStatusLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        discountStatusLabel.setForeground(DISCOUNT_GREEN);

        statusPanel.add(transactionStatusLabel, BorderLayout.WEST);
        statusPanel.add(discountStatusLabel, BorderLayout.EAST);

        panel.add(statusPanel, BorderLayout.NORTH);
        panel.add(createTotalsPanel(), BorderLayout.CENTER);
        panel.add(createQuickKeysPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBackground(BACKGROUND_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 200));

        subtotalLabel = createTotalLabel();
        taxLabel = createTotalLabel();
        totalLabel = createTotalLabel();

        // Discount row (initially hidden)
        discountPanel = new JPanel(new BorderLayout());
        discountPanel.setBackground(BACKGROUND_GRAY);
        discountLabel = new JLabel("DISCOUNT:");
        discountLabel.setFont(new Font("Arial", Font.BOLD, 28));
        discountLabel.setForeground(DISCOUNT_GREEN);
        discountAmountLabel = new JLabel("-$0.00");
        discountAmountLabel.setFont(new Font("Arial", Font.BOLD, 28));
        discountAmountLabel.setForeground(DISCOUNT_GREEN);
        discountAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        discountPanel.add(discountLabel, BorderLayout.WEST);
        discountPanel.add(discountAmountLabel, BorderLayout.EAST);
        discountPanel.setVisible(false);

        panel.add(createTotalRow("SUBTOTAL:", subtotalLabel));
        panel.add(discountPanel);
        panel.add(createTotalRow("TAX (7%):", taxLabel));
        panel.add(createTotalRow("TOTAL:", totalLabel));

        return panel;
    }

    private JPanel createTotalRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BACKGROUND_GRAY);

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(textLabel, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);

        return row;
    }

    private JLabel createTotalLabel() {
        JLabel label = new JLabel("$0.00");
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JPanel createQuickKeysPanel() {
        quickKeysPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        quickKeysPanel.setBackground(Color.WHITE);
        quickKeysPanel.setPreferredSize(new Dimension(500, 0));
        quickKeysPanel.setBorder(BorderFactory.createTitledBorder("Quick Keys"));

        addQuickKey("041594904794", "Polar Pop 42oz", 8.91);
        addQuickKey("999999955678", "Hot Dog", 2.69);
        addQuickKey("999991218948", "Large Coffee", 2.19);
        addQuickKey("999999937551", "Medium Polar Pop", 0.89);
        addQuickKey("028200003843", "Marlboro Gold", 8.47);
        addQuickKey("070847811169", "Monster Energy", 3.29);
        addQuickKey("012000001314", "Mt Dew 20oz", 9.96);
        addQuickKey("049000000443", "Donut", 2.49);

        return quickKeysPanel;
    }

    private void addQuickKey(String upc, String name, double price) {
        JButton btn = new JButton("<html><center>" + truncate(name, 15) + "<br>$" + df.format(price) + "</center></html>");
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(BUTTON_BLUE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> controller.addItem(upc, 1));
        quickKeysPanel.add(btn);
    }

    // ==================== Center Panel ====================

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(createTablePanel(), BorderLayout.CENTER);
        panel.add(createActionButtonsPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        itemTable = createItemTable();
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createItemTable() {
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(200, 200, 200));
        table.setBackground(BACKGROUND_GRAY);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {"UPC", "Description", "Price", "QTY", "Total"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 8));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createActionButton("Void Item", BUTTON_RED, e -> handleVoidItem()));
        panel.add(createActionButton("Qty Change", BUTTON_BLUE, e -> handleQuantityChange()));
        panel.add(createActionButton("Void Trans", BUTTON_RED, e -> handleVoidTransaction()));
        panel.add(createActionButton("Suspend", BUTTON_YELLOW, e -> handleSuspendTransaction()));
        panel.add(createActionButton("Resume", BUTTON_YELLOW, e -> handleResumeTransaction()));
        panel.add(createActionButton("Discounts", BUTTON_PURPLE, e -> handleApplyDiscounts()));
        panel.add(createActionButton("History", BUTTON_BLUE, e -> handleShowHistory()));

        // Pay button - prominent green button
        payButton = createActionButton("PAY", BUTTON_GREEN, e -> handlePayment());
        payButton.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(payButton);

        return panel;
    }

    private JButton createActionButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        return btn;
    }

    // ==================== Bottom Panel ====================

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBackground(Color.WHITE);

        JLabel upcLabel = new JLabel("UPC:");
        upcLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        panel.add(upcLabel);
        panel.add(createUpcInput());

        JLabel qtyLabel = new JLabel("QTY:");
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        panel.add(qtyLabel);
        panel.add(createQtyInput());
        panel.add(createAddButton());

        return panel;
    }

    private JTextField createUpcInput() {
        upcInput = new JTextField(20);
        upcInput.setFont(new Font("Arial", Font.PLAIN, 18));
        upcInput.setPreferredSize(new Dimension(300, 45));
        upcInput.addActionListener(e -> handleAddItem());
        return upcInput;
    }

    private JTextField createQtyInput() {
        qtyInput = new JTextField("1");
        qtyInput.setFont(new Font("Arial", Font.PLAIN, 18));
        qtyInput.setPreferredSize(new Dimension(80, 45));
        qtyInput.addActionListener(e -> handleAddItem());
        return qtyInput;
    }

    private JButton createAddButton() {
        JButton btn = new JButton("ADD");
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(120, 45));
        btn.setBackground(BUTTON_GREEN);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> handleAddItem());
        return btn;
    }

    // ==================== Event Handlers ====================

    private void handleAddItem() {
        String upc = upcInput.getText().trim();
        if (upc.isEmpty()) {
            showError("Please enter a UPC code");
            return;
        }
        int qty = parseQuantity();
        if (qty <= 0) return;
        controller.addItem(upc, qty);
        clearInputs();
    }

    private void handleVoidItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an item to void");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Void selected item?", "Confirm Void", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidItem(selectedRow);
        }
    }

    private void handleQuantityChange() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an item");
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Enter new quantity:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int newQty = Integer.parseInt(input.trim());
                if (newQty > 0) {
                    controller.changeQuantity(selectedRow, newQty);
                } else {
                    showError("Quantity must be greater than 0");
                }
            } catch (NumberFormatException e) {
                showError("Invalid quantity");
            }
        }
    }

    private void handleVoidTransaction() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No transaction to void");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Void entire transaction?", "Confirm Void Transaction", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidTransaction();
        }
    }

    private void handleSuspendTransaction() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No transaction to suspend");
            return;
        }
        controller.suspendTransaction();
    }

    private void handleResumeTransaction() {
        controller.resumeTransaction();
    }

    private void handleApplyDiscounts() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No items in transaction");
            return;
        }
        controller.applyDiscounts();
    }

    private void handleShowHistory() {
        controller.showTransactionHistory();
    }

    private void handlePayment() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No items in transaction");
            return;
        }
        // Trigger navigation to payment panel
        onPaymentRequested.run();
    }

    // ==================== Public UI Update Methods ====================

    public void addItemToTable(String upc, String desc, double price, int qty, double total) {
        Object[] row = {upc, truncate(desc, 40), df.format(price), String.valueOf(qty), df.format(total)};
        tableModel.addRow(row);
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void updateTotals(double subtotal, double discount, double tax, double total) {
        subtotalLabel.setText("$" + df.format(subtotal));
        taxLabel.setText("$" + df.format(tax));
        totalLabel.setText("$" + df.format(total));

        if (discount > 0) {
            discountAmountLabel.setText("-$" + df.format(discount));
            discountPanel.setVisible(true);
        } else {
            discountPanel.setVisible(false);
        }
    }

    public void setDiscountStatus(List<String> appliedDiscounts) {
        if (appliedDiscounts != null && !appliedDiscounts.isEmpty()) {
            discountStatusLabel.setText("✓ " + String.join(", ", appliedDiscounts));
        } else {
            discountStatusLabel.setText("");
        }
    }

    public void setTransactionStatus(String status) {
        transactionStatusLabel.setText(status);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public JTextField getUpcInput() {
        return upcInput;
    }

    public JTextField getQtyInput() {
        return qtyInput;
    }

    // ==================== Helper Methods ====================

    private int parseQuantity() {
        try {
            int qty = Integer.parseInt(qtyInput.getText().trim());
            if (qty <= 0) {
                showError("Quantity must be greater than 0");
                return -1;
            }
            return qty;
        } catch (NumberFormatException e) {
            showError("Invalid quantity");
            return -1;
        }
    }

    private void clearInputs() {
        upcInput.setText("");
        qtyInput.setText("1");
        upcInput.requestFocus();
    }

    public void flashUpcField(String upc) {
        Color flashGreen = new Color(144, 238, 144, 100);
        upcInput.setText(upc);
        upcInput.setBackground(flashGreen);
        Timer timer = new Timer(200, evt -> {
            upcInput.setText("");
            upcInput.setBackground(Color.WHITE);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}