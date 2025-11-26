import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Panel for processing payments - displays order summary and payment options.
 */
public class PaymentPanel extends JPanel {

    // Color constants
    private static final Color BACKGROUND_GRAY = new Color(220, 220, 220);
    private static final Color BUTTON_GREEN = new Color(119, 214, 135);
    private static final Color BUTTON_RED = new Color(220, 100, 100);
    private static final Color BUTTON_BLUE = new Color(100, 150, 220);
    private static final Color DISCOUNT_GREEN = new Color(0, 150, 0);
    private static final Color TOTAL_BACKGROUND = new Color(45, 45, 45);

    // Components
    private final RegisterController controller;
    private final Runnable onBackRequested;
    private final Runnable onPaymentComplete;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // UI Elements
    private DefaultTableModel summaryTableModel;
    private JLabel subtotalValueLabel;
    private JLabel discountValueLabel;
    private JLabel taxValueLabel;
    private JLabel totalValueLabel;
    private JPanel discountRow;
    private JLabel tenderedLabel;
    private JLabel changeLabel;
    private JPanel changePanel;

    public PaymentPanel(RegisterController controller, Runnable onBackRequested, Runnable onPaymentComplete) {
        this.controller = controller;
        this.onBackRequested = onBackRequested;
        this.onPaymentComplete = onPaymentComplete;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(250, 250, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // ==================== Header Panel ====================

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel("Payment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(50, 50, 50));

        panel.add(titleLabel, BorderLayout.WEST);
        return panel;
    }

    // ==================== Main Content Panel ====================

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(250, 250, 250));

        panel.add(createOrderSummaryPanel());
        panel.add(createPaymentOptionsPanel());

        return panel;
    }

    private JPanel createOrderSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel title = new JLabel("Order Summary");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        // Items table
        panel.add(createSummaryTable(), BorderLayout.CENTER);

        // Totals section
        panel.add(createTotalsSection(), BorderLayout.SOUTH);

        return panel;
    }

    private JScrollPane createSummaryTable() {
        String[] columns = {"Item", "Qty", "Price"};
        summaryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable summaryTable = new JTable(summaryTableModel);
        summaryTable.setFont(new Font("Arial", Font.PLAIN, 14));
        summaryTable.setRowHeight(28);
        summaryTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        summaryTable.getTableHeader().setBackground(BACKGROUND_GRAY);
        summaryTable.setBackground(Color.WHITE);

        // Set column widths
        summaryTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        summaryTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        summaryTable.getColumnModel().getColumn(2).setPreferredWidth(80);

        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        return scrollPane;
    }

    private JPanel createTotalsSection() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Subtotal row
        JPanel subtotalRow = createTotalRow("Subtotal:", false);
        subtotalValueLabel = (JLabel) subtotalRow.getComponent(1);
        panel.add(subtotalRow);

        // Discount row (conditionally visible)
        discountRow = createTotalRow("Discount:", false);
        discountRow.setVisible(false);
        discountValueLabel = (JLabel) discountRow.getComponent(1);
        discountValueLabel.setForeground(DISCOUNT_GREEN);
        discountRow.getComponent(0).setForeground(DISCOUNT_GREEN);
        panel.add(discountRow);

        // Tax row
        JPanel taxRow = createTotalRow("Tax (7%):", false);
        taxValueLabel = (JLabel) taxRow.getComponent(1);
        panel.add(taxRow);

        // Separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        panel.add(separator);

        // Total row (emphasized)
        JPanel totalRow = createTotalRow("TOTAL:", true);
        totalValueLabel = (JLabel) totalRow.getComponent(1);
        panel.add(totalRow);

        return panel;
    }

    private JPanel createTotalRow(String labelText, boolean emphasized) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        JLabel value = new JLabel("$0.00");
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        if (emphasized) {
            label.setFont(new Font("Arial", Font.BOLD, 24));
            value.setFont(new Font("Arial", Font.BOLD, 24));
        } else {
            label.setFont(new Font("Arial", Font.PLAIN, 16));
            value.setFont(new Font("Arial", Font.PLAIN, 16));
        }

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        return row;
    }

    // ==================== Payment Options Panel ====================

    private JPanel createPaymentOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Title
        JLabel title = new JLabel("Payment Method");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        // Payment buttons
        panel.add(createPaymentButtonsPanel(), BorderLayout.CENTER);

        // Change display panel
        panel.add(createChangeDisplayPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPaymentButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Cash section
        JPanel cashSection = new JPanel(new BorderLayout(5, 10));
        cashSection.setBackground(Color.WHITE);

        JLabel cashLabel = new JLabel("Cash");
        cashLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cashSection.add(cashLabel, BorderLayout.NORTH);

        JPanel cashButtonsGrid = new JPanel(new GridLayout(2, 4, 8, 8));
        cashButtonsGrid.setBackground(Color.WHITE);

        cashButtonsGrid.add(createCashButton("$5", 5.0));
        cashButtonsGrid.add(createCashButton("$10", 10.0));
        cashButtonsGrid.add(createCashButton("$20", 20.0));
        cashButtonsGrid.add(createCashButton("$50", 50.0));
        cashButtonsGrid.add(createCashButton("$100", 100.0));
        cashButtonsGrid.add(createExactButton());
        cashButtonsGrid.add(createNextDollarButton());
        cashButtonsGrid.add(createCustomButton());

        cashSection.add(cashButtonsGrid, BorderLayout.CENTER);
        panel.add(cashSection, BorderLayout.NORTH);

        // Card section
        JPanel cardSection = new JPanel(new BorderLayout(5, 10));
        cardSection.setBackground(Color.WHITE);
        cardSection.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel cardLabel = new JLabel("Card");
        cardLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cardSection.add(cardLabel, BorderLayout.NORTH);

        JButton cardButton = new JButton("CREDIT / DEBIT CARD");
        cardButton.setFont(new Font("Arial", Font.BOLD, 18));
        cardButton.setBackground(BUTTON_BLUE);
        cardButton.setForeground(Color.WHITE);
        cardButton.setFocusPainted(false);
        cardButton.setPreferredSize(new Dimension(0, 60));
        cardButton.addActionListener(e -> handleCardPayment());
        cardSection.add(cardButton, BorderLayout.CENTER);

        panel.add(cardSection, BorderLayout.CENTER);

        return panel;
    }

    private JButton createCashButton(String text, double amount) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(BUTTON_GREEN);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(80, 50));
        btn.addActionListener(e -> handleCashPayment(amount));
        return btn;
    }

    private JButton createExactButton() {
        JButton btn = new JButton("EXACT");
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(100, 180, 100));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> handleCashPayment(controller.getTotal()));
        return btn;
    }

    private JButton createNextDollarButton() {
        JButton btn = new JButton("NEXT $");
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(100, 180, 100));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            double nextDollar = Math.ceil(controller.getTotal());
            handleCashPayment(nextDollar);
        });
        return btn;
    }

    private JButton createCustomButton() {
        JButton btn = new JButton("OTHER");
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(new Color(180, 180, 180));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> handleCustomAmount());
        return btn;
    }

    private JPanel createChangeDisplayPanel() {
        changePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        changePanel.setBackground(TOTAL_BACKGROUND);
        changePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        changePanel.setVisible(false);

        tenderedLabel = new JLabel("Tendered: $0.00");
        tenderedLabel.setFont(new Font("Arial", Font.BOLD, 18));
        tenderedLabel.setForeground(Color.WHITE);

        changeLabel = new JLabel("Change: $0.00");
        changeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        changeLabel.setForeground(new Color(144, 238, 144));

        changePanel.add(tenderedLabel);
        changePanel.add(changeLabel);

        return changePanel;
    }

    // ==================== Bottom Panel ====================

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Back button
        JButton backButton = new JButton("← Back to Transaction");
        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
        backButton.setBackground(new Color(240, 240, 240));
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(200, 45));
        backButton.addActionListener(e -> onBackRequested.run());

        // Cancel button
        JButton cancelButton = new JButton("Cancel Transaction");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 16));
        cancelButton.setBackground(BUTTON_RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(180, 45));
        cancelButton.addActionListener(e -> handleCancelTransaction());

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtons.setBackground(new Color(250, 250, 250));
        leftButtons.add(backButton);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtons.setBackground(new Color(250, 250, 250));
        rightButtons.add(cancelButton);

        panel.add(leftButtons, BorderLayout.WEST);
        panel.add(rightButtons, BorderLayout.EAST);

        return panel;
    }

    // ==================== Payment Handlers ====================

    private void handleCashPayment(double tendered) {
        double total = controller.getTotal();

        if (tendered < total) {
            showError("Insufficient amount. Need at least $" + df.format(total));
            return;
        }

        double change = tendered - total;

        // Show change display
        tenderedLabel.setText("Tendered: $" + df.format(tendered));
        changeLabel.setText("Change: $" + df.format(change));
        changePanel.setVisible(true);

        // Confirm and complete
        int confirm = JOptionPane.showConfirmDialog(this,
                "Complete transaction?\n\nTendered: $" + df.format(tendered) +
                        "\nChange: $" + df.format(change),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.completeTransaction("CASH", tendered);
            onPaymentComplete.run();
        }

        changePanel.setVisible(false);
    }

    private void handleCustomAmount() {
        String input = JOptionPane.showInputDialog(this,
                "Enter amount tendered:",
                "Custom Amount",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                handleCashPayment(amount);
            } catch (NumberFormatException e) {
                showError("Invalid amount entered");
            }
        }
    }

    private void handleCardPayment() {
        double total = controller.getTotal();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Process card payment for $" + df.format(total) + "?",
                "Confirm Card Payment",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.completeTransaction("CREDIT", total);
            onPaymentComplete.run();
        }
    }

    private void handleCancelTransaction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel this transaction?",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidTransaction();
            onBackRequested.run();
        }
    }

    // ==================== Public Methods ====================

    /**
     * Refreshes the payment panel with current transaction data.
     * Should be called when switching to this panel.
     */
    public void refresh() {
        // Clear and populate summary table
        summaryTableModel.setRowCount(0);
        List<TransactionItem> items = controller.getCurrentTransaction();

        for (TransactionItem item : items) {
            Object[] row = {
                    truncate(item.getProduct().getName()),
                    item.getQuantity(),
                    "$" + df.format(item.getTotal())
            };
            summaryTableModel.addRow(row);
        }

        // Update totals
        double subtotal = controller.getSubtotal();
        double discount = controller.getDiscountAmount();
        double tax = controller.getTax();
        double total = controller.getTotal();

        subtotalValueLabel.setText("$" + df.format(subtotal));
        taxValueLabel.setText("$" + df.format(tax));
        totalValueLabel.setText("$" + df.format(total));

        if (discount > 0) {
            discountValueLabel.setText("-$" + df.format(discount));
            discountRow.setVisible(true);
        } else {
            discountRow.setVisible(false);
        }

        // Reset change panel
        changePanel.setVisible(false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String truncate(String text) {
        if (text.length() <= 35) return text;
        return text.substring(0, 35 - 3) + "...";
    }
}