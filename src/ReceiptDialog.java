// src/ReceiptDialog.java

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Modern Receipt Dialog displayed after completing a transaction.
 * Shows a styled receipt with all transaction details.
 */
public class ReceiptDialog extends JDialog {

    // Modern Color Scheme
    private static final Color PRIMARY_BG = new Color(245, 247, 250);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color RECEIPT_BG = new Color(255, 255, 255);
    private static final Color DISCOUNT_GREEN = new Color(5, 150, 105);

    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

    public ReceiptDialog(Frame parent, int transactionId, List<TransactionItem> items,
                         double subtotal, double discount, double tax, double total,
                         String paymentType, double tendered, double change) {
        super(parent, "Receipt", true);

        setSize(500, 700);
        setLocationRelativeTo(parent);
        setResizable(false);

        initializeUI(transactionId, items, subtotal, discount, tax, total, paymentType, tendered, change);
    }

    private void initializeUI(int transactionId, List<TransactionItem> items,
                              double subtotal, double discount, double tax, double total,
                              String paymentType, double tendered, double change) {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(PRIMARY_BG);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(PRIMARY_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Receipt panel (the actual receipt look)
        JPanel receiptPanel = createReceiptPanel(transactionId, items, subtotal, discount, tax, total, paymentType, tendered, change);

        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();

        mainPanel.add(receiptPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // ==================== Receipt Panel ====================

    private JPanel createReceiptPanel(int transactionId, List<TransactionItem> items,
                                      double subtotal, double discount, double tax, double total,
                                      String paymentType, double tendered, double change) {
        JPanel receiptContainer = new JPanel(new BorderLayout());
        receiptContainer.setBackground(RECEIPT_BG);
        receiptContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2, true),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        // Content panel with vertical layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(RECEIPT_BG);

        // Header
        contentPanel.add(createReceiptHeader(transactionId));
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createDivider());
        contentPanel.add(Box.createVerticalStrut(15));

        // Items
        contentPanel.add(createItemsSection(items));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createDivider());
        contentPanel.add(Box.createVerticalStrut(15));

        // Totals
        contentPanel.add(createTotalsSection(subtotal, discount, tax, total));
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(createThickDivider());
        contentPanel.add(Box.createVerticalStrut(15));

        // Payment details
        contentPanel.add(createPaymentSection(paymentType, tendered, change));
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(createDivider());
        contentPanel.add(Box.createVerticalStrut(15));

        // Footer
        contentPanel.add(createReceiptFooter());

        // Wrap in scroll pane for long receipts
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(RECEIPT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        receiptContainer.add(scrollPane, BorderLayout.CENTER);

        return receiptContainer;
    }

    // ==================== Receipt Header ====================

    private JPanel createReceiptHeader(int transactionId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RECEIPT_BG);

        // Store name
        JLabel storeLabel = new JLabel("MOCK REGISTER");
        storeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        storeLabel.setForeground(TEXT_PRIMARY);
        storeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel addressLabel = new JLabel("123 Main Street, City, ST 12345");
        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        addressLabel.setForeground(TEXT_SECONDARY);
        addressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel phoneLabel = new JLabel("Tel: (555) 123-4567");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phoneLabel.setForeground(TEXT_SECONDARY);
        phoneLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(storeLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(addressLabel);
        panel.add(phoneLabel);
        panel.add(Box.createVerticalStrut(15));

        // Transaction info
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_SECONDARY);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel transLabel = new JLabel("Transaction #" + transactionId);
        transLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        transLabel.setForeground(TEXT_PRIMARY);
        transLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(dateLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(transLabel);

        return panel;
    }

    // ==================== Items Section ====================

    private JPanel createItemsSection(List<TransactionItem> items) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RECEIPT_BG);

        for (TransactionItem item : items) {
            panel.add(createItemRow(item));
            panel.add(Box.createVerticalStrut(8));
        }

        return panel;
    }

    private JPanel createItemRow(TransactionItem item) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setBackground(RECEIPT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Item name and quantity
        String itemText = truncate(item.getProduct().getName());
        JLabel nameLabel = new JLabel(itemText);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setForeground(TEXT_PRIMARY);

        // Quantity and price details
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBackground(RECEIPT_BG);

        String qtyPrice = item.getQuantity() + " @ $" + df.format(item.getProduct().getPrice());
        JLabel qtyLabel = new JLabel(qtyPrice);
        qtyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        qtyLabel.setForeground(TEXT_SECONDARY);

        JLabel totalLabel = new JLabel("$" + df.format(item.getTotal()));
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalLabel.setForeground(TEXT_PRIMARY);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(RECEIPT_BG);
        leftPanel.add(nameLabel);
        leftPanel.add(qtyLabel);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(totalLabel, BorderLayout.EAST);

        return row;
    }

    // ==================== Totals Section ====================

    private JPanel createTotalsSection(double subtotal, double discount, double tax, double total) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RECEIPT_BG);

        // Subtotal
        panel.add(createTotalRow("Subtotal:", subtotal, false, TEXT_SECONDARY));
        panel.add(Box.createVerticalStrut(8));

        // Discount (if applicable)
        if (discount > 0) {
            panel.add(createDiscountRow(discount));
            panel.add(Box.createVerticalStrut(8));
        }

        // Tax
        panel.add(createTotalRow("Tax (7%):", tax, false, TEXT_SECONDARY));
        panel.add(Box.createVerticalStrut(12));

        // Total (emphasized)
        panel.add(createTotalRow("TOTAL:", total, true, TEXT_PRIMARY));

        return panel;
    }

    private JPanel createTotalRow(String label, double amount, boolean emphasized, Color labelColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(RECEIPT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, emphasized ? 35 : 25));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", emphasized ? Font.BOLD : Font.PLAIN, emphasized ? 16 : 13));
        labelComp.setForeground(labelColor);

        JLabel amountComp = new JLabel("$" + df.format(amount));
        amountComp.setFont(new Font("Segoe UI", Font.BOLD, emphasized ? 20 : 13));
        amountComp.setForeground(emphasized ? ACCENT_GREEN : TEXT_PRIMARY);
        amountComp.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComp, BorderLayout.WEST);
        row.add(amountComp, BorderLayout.EAST);

        return row;
    }

    private JPanel createDiscountRow(double amount) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(RECEIPT_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel labelComp = new JLabel("Discount:");
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelComp.setForeground(DISCOUNT_GREEN);

        JLabel amountComp = new JLabel("-$" + df.format(amount));
        amountComp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountComp.setForeground(DISCOUNT_GREEN);
        amountComp.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComp, BorderLayout.WEST);
        row.add(amountComp, BorderLayout.EAST);

        return row;
    }

    // ==================== Payment Section ====================

    private JPanel createPaymentSection(String paymentType, double tendered, double change) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RECEIPT_BG);

        // Payment type
        JPanel typeRow = new JPanel(new BorderLayout());
        typeRow.setBackground(RECEIPT_BG);
        typeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel typeLabel = new JLabel("Payment Method:");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeLabel.setForeground(TEXT_SECONDARY);

        JLabel typeValue = new JLabel(paymentType);
        typeValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeValue.setForeground(TEXT_PRIMARY);
        typeValue.setHorizontalAlignment(SwingConstants.RIGHT);

        typeRow.add(typeLabel, BorderLayout.WEST);
        typeRow.add(typeValue, BorderLayout.EAST);

        panel.add(typeRow);

        // For cash payments, show tendered and change
        if (paymentType.equalsIgnoreCase("CASH")) {
            panel.add(Box.createVerticalStrut(8));

            // Tendered
            JPanel tenderedRow = new JPanel(new BorderLayout());
            tenderedRow.setBackground(RECEIPT_BG);
            tenderedRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

            JLabel tenderedLabel = new JLabel("Tendered:");
            tenderedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tenderedLabel.setForeground(TEXT_SECONDARY);

            JLabel tenderedValue = new JLabel("$" + df.format(tendered));
            tenderedValue.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tenderedValue.setForeground(TEXT_PRIMARY);
            tenderedValue.setHorizontalAlignment(SwingConstants.RIGHT);

            tenderedRow.add(tenderedLabel, BorderLayout.WEST);
            tenderedRow.add(tenderedValue, BorderLayout.EAST);

            panel.add(tenderedRow);
            panel.add(Box.createVerticalStrut(8));

            // Change
            JPanel changeRow = new JPanel(new BorderLayout());
            changeRow.setBackground(RECEIPT_BG);
            changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel changeLabel = new JLabel("Change:");
            changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            changeLabel.setForeground(TEXT_PRIMARY);

            JLabel changeValue = new JLabel("$" + df.format(change));
            changeValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
            changeValue.setForeground(ACCENT_GREEN);
            changeValue.setHorizontalAlignment(SwingConstants.RIGHT);

            changeRow.add(changeLabel, BorderLayout.WEST);
            changeRow.add(changeValue, BorderLayout.EAST);

            panel.add(changeRow);
        }

        return panel;
    }

    // ==================== Receipt Footer ====================

    private JPanel createReceiptFooter() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RECEIPT_BG);

        JLabel thankYouLabel = new JLabel("Thank You For Your Business!");
        thankYouLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        thankYouLabel.setForeground(TEXT_PRIMARY);
        thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel visitLabel = new JLabel("Please Visit Again");
        visitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        visitLabel.setForeground(TEXT_SECONDARY);
        visitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(thankYouLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(visitLabel);

        return panel;
    }

    // ==================== Dividers ====================

    private JSeparator createDivider() {
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(BORDER_COLOR);
        return separator;
    }

    private JSeparator createThickDivider() {
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        separator.setForeground(TEXT_SECONDARY);
        return separator;
    }

    // ==================== Buttons Panel ====================

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(PRIMARY_BG);

        JButton printButton = createStyledButton("Print Receipt", ACCENT_GREEN);
        printButton.addActionListener(e -> handlePrint());

        JButton closeButton = createStyledButton("Close", new Color(107, 114, 128));
        closeButton.addActionListener(e -> dispose());

        panel.add(closeButton);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 45));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    // ==================== Event Handlers ====================

    private void handlePrint() {
        // Placeholder for print functionality
        JOptionPane.showMessageDialog(this,
                "Print functionality would be implemented here.\nReceipt would be sent to the default printer.",
                "Print Receipt",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== Helper Methods ====================

    private String truncate(String text) {
        if (text.length() <= 30) return text;
        return text.substring(0, 30 - 3) + "...";
    }
}