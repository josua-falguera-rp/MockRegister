// src/PaymentPanel.java

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Revamped Payment Panel with modern UI design matching TransactionPanel.
 * Displays order summary and payment options with improved visual hierarchy.
 */
public class PaymentPanel extends JPanel {

    // Modern Color Scheme (matching TransactionPanel)
    private static final Color PRIMARY_BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_ORANGE = new Color(251, 146, 60);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color DISCOUNT_GREEN = new Color(5, 150, 105);
    private static final Color TABLE_HEADER = new Color(243, 244, 246);
    private static final Color TOTAL_HIGHLIGHT = new Color(16, 185, 129);

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
    private JPanel discountCard;
    private JPanel changeDisplayPanel;
    private JLabel tenderedLabel;
    private JLabel changeLabel;

    public PaymentPanel(RegisterController controller, Runnable onBackRequested, Runnable onPaymentComplete) {
        this.controller = controller;
        this.onBackRequested = onBackRequested;
        this.onPaymentComplete = onPaymentComplete;

        setLayout(new BorderLayout(20, 20));
        setBackground(PRIMARY_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ==================== Header Panel ====================

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 10));
        panel.setBackground(PRIMARY_BG);

        JLabel titleLabel = new JLabel("Payment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Review order and select payment method");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBackground(PRIMARY_BG);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    // ==================== Main Content Panel ====================

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(PRIMARY_BG);

        panel.add(createOrderSummaryPanel());
        panel.add(createPaymentOptionsPanel());

        return panel;
    }

    // ==================== Order Summary Panel ====================

    private JPanel createOrderSummaryPanel() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(PRIMARY_BG);

        // Items section
        container.add(createItemsSection(), BorderLayout.CENTER);

        // Totals section
        container.add(createTotalsSection(), BorderLayout.SOUTH);

        return container;
    }

    private JPanel createItemsSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel headerLabel = new JLabel("📋 Order Summary");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_PRIMARY);

        // Create table
        String[] columns = {"Item", "Qty", "Price"};
        summaryTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable summaryTable = createStyledTable();
        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable() {
        JTable table = new JTable(summaryTableModel);

        // Modern table styling
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(10, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(CARD_BG);
                }
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return c;
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        return table;
    }

    private JPanel createTotalsSection() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(PRIMARY_BG);

        // Subtotal Card
        container.add(createTotalCard("Subtotal", "$0.00", TEXT_SECONDARY, false));
        container.add(Box.createVerticalStrut(10));

        // Discount Card (hidden initially)
        discountCard = createTotalCard("Discount", "-$0.00", DISCOUNT_GREEN, false);
        discountCard.setVisible(false);
        container.add(discountCard);

        // Tax Card
        container.add(createTotalCard("Tax (7%)", "$0.00", TEXT_SECONDARY, false));
        container.add(Box.createVerticalStrut(15));

        // Total Card (emphasized)
        container.add(createTotalCard("TOTAL", "$0.00", TOTAL_HIGHLIGHT, true));

        return container;
    }

    private JPanel createTotalCard(String label, String value, Color valueColor, boolean emphasized) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(emphasized ? TOTAL_HIGHLIGHT : BORDER_COLOR, emphasized ? 2 : 1, true),
                BorderFactory.createEmptyBorder(emphasized ? 20 : 15, 20, emphasized ? 20 : 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, emphasized ? 100 : 70));

        // Label
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", emphasized ? Font.BOLD : Font.PLAIN, emphasized ? 18 : 15));
        labelComp.setForeground(emphasized ? TEXT_PRIMARY : TEXT_SECONDARY);

        // Value
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, emphasized ? 36 : 22));
        valueComp.setForeground(valueColor);
        valueComp.setHorizontalAlignment(SwingConstants.RIGHT);

        // Store references
        if (label.equals("Subtotal")) subtotalValueLabel = valueComp;
        else if (label.equals("Discount")) discountValueLabel = valueComp;
        else if (label.contains("Tax")) taxValueLabel = valueComp;
        else if (label.equals("TOTAL")) totalValueLabel = valueComp;

        card.add(labelComp, BorderLayout.WEST);
        card.add(valueComp, BorderLayout.EAST);

        return card;
    }

    // ==================== Payment Options Panel ====================

    private JPanel createPaymentOptionsPanel() {
        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(PRIMARY_BG);

        container.add(createPaymentMethodsSection(), BorderLayout.CENTER);
        container.add(createChangeDisplaySection(), BorderLayout.SOUTH);

        return container;
    }

    private JPanel createPaymentMethodsSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel headerLabel = new JLabel("💰 Payment Method");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_PRIMARY);

        // Payment options
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(CARD_BG);

        // Cash Section
        optionsPanel.add(createCashSection());
        optionsPanel.add(Box.createVerticalStrut(20));

        // Card Section
        optionsPanel.add(createCardSection());

        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCashSection() {
        JPanel section = new JPanel(new BorderLayout(10, 12));
        section.setBackground(CARD_BG);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel sectionLabel = new JLabel("💵 Cash");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionLabel.setForeground(TEXT_PRIMARY);

        // Quick amount buttons
        JPanel quickAmounts = new JPanel(new GridLayout(2, 4, 10, 10));
        quickAmounts.setBackground(CARD_BG);

        quickAmounts.add(createCashButton("$5", 5.0));
        quickAmounts.add(createCashButton("$10", 10.0));
        quickAmounts.add(createCashButton("$20", 20.0));
        quickAmounts.add(createCashButton("$50", 50.0));
        quickAmounts.add(createCashButton("$100", 100.0));
        quickAmounts.add(createExactButton());
        quickAmounts.add(createNextDollarButton());
        quickAmounts.add(createCustomButton());

        section.add(sectionLabel, BorderLayout.NORTH);
        section.add(quickAmounts, BorderLayout.CENTER);

        return section;
    }

    private JButton createCashButton(String text, double amount) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(ACCENT_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 55));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_GREEN.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(ACCENT_GREEN);
            }
        });

        btn.addActionListener(e -> handleCashPayment(amount));
        return btn;
    }

    private JButton createExactButton() {
        JButton btn = new JButton("EXACT");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(5, 150, 105));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(5, 150, 105).darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(5, 150, 105));
            }
        });

        btn.addActionListener(e -> handleCashPayment(controller.getTotal()));
        return btn;
    }

    private JButton createNextDollarButton() {
        JButton btn = new JButton("NEXT $");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(5, 150, 105));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(5, 150, 105).darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(5, 150, 105));
            }
        });

        btn.addActionListener(e -> {
            double nextDollar = Math.ceil(controller.getTotal());
            handleCashPayment(nextDollar);
        });
        return btn;
    }

    private JButton createCustomButton() {
        JButton btn = new JButton("OTHER");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(107, 114, 128));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(107, 114, 128).darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(107, 114, 128));
            }
        });

        btn.addActionListener(e -> handleCustomAmount());
        return btn;
    }

    private JPanel createCardSection() {
        JPanel section = new JPanel(new BorderLayout(10, 12));
        section.setBackground(CARD_BG);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel sectionLabel = new JLabel("💳 Card Payment");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sectionLabel.setForeground(TEXT_PRIMARY);

        JButton cardButton = new JButton("CREDIT / DEBIT CARD");
        cardButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cardButton.setBackground(ACCENT_BLUE);
        cardButton.setForeground(Color.WHITE);
        cardButton.setFocusPainted(false);
        cardButton.setBorderPainted(false);
        cardButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cardButton.setPreferredSize(new Dimension(0, 65));

        cardButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cardButton.setBackground(ACCENT_BLUE.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardButton.setBackground(ACCENT_BLUE);
            }
        });

        cardButton.addActionListener(e -> handleCardPayment());

        section.add(sectionLabel, BorderLayout.NORTH);
        section.add(cardButton, BorderLayout.CENTER);

        return section;
    }

    private JPanel createChangeDisplaySection() {
        changeDisplayPanel = new JPanel(new BorderLayout(15, 10));
        changeDisplayPanel.setBackground(CARD_BG);
        changeDisplayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 2, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        changeDisplayPanel.setVisible(false);

        JPanel labelsPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        labelsPanel.setBackground(CARD_BG);

        tenderedLabel = new JLabel("Tendered: $0.00");
        tenderedLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tenderedLabel.setForeground(TEXT_PRIMARY);

        changeLabel = new JLabel("Change: $0.00");
        changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        changeLabel.setForeground(ACCENT_GREEN);

        labelsPanel.add(tenderedLabel);
        labelsPanel.add(changeLabel);

        JLabel iconLabel = new JLabel("💵");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        changeDisplayPanel.add(iconLabel, BorderLayout.WEST);
        changeDisplayPanel.add(labelsPanel, BorderLayout.CENTER);

        return changeDisplayPanel;
    }

    // ==================== Footer Panel ====================

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(PRIMARY_BG);

        // Back button
        JButton backButton = createStyledButton("← Back to Transaction", new Color(107, 114, 128));
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.addActionListener(e -> onBackRequested.run());

        // Cancel button
        JButton cancelButton = createStyledButton("Cancel Transaction", ACCENT_RED);
        cancelButton.setPreferredSize(new Dimension(200, 50));
        cancelButton.addActionListener(e -> handleCancelTransaction());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(PRIMARY_BG);
        leftPanel.add(backButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setBackground(PRIMARY_BG);
        rightPanel.add(cancelButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

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
        changeDisplayPanel.setVisible(true);

        // Styled confirmation dialog
        JPanel confirmPanel = createConfirmationPanel(tendered, change);

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmPanel,
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.completeTransaction("CASH", tendered);
            onPaymentComplete.run();
        }

        changeDisplayPanel.setVisible(false);
    }

    private JPanel createConfirmationPanel(double tendered, double change) {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("💵 Complete Cash Payment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_GREEN);

        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 5, 10));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel tenderedLbl = new JLabel("Tendered: $" + df.format(tendered));
        tenderedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tenderedLbl.setForeground(TEXT_PRIMARY);

        JLabel changeLbl = new JLabel("Change: $" + df.format(change));
        changeLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        changeLbl.setForeground(ACCENT_GREEN);

        detailsPanel.add(tenderedLbl);
        detailsPanel.add(changeLbl);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);

        return panel;
    }

    private void handleCustomAmount() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 15));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("💵 Enter Custom Amount", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_GREEN);

        JLabel instructionLabel = new JLabel("Total Due: $" + df.format(controller.getTotal()));
        instructionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        instructionLabel.setForeground(TEXT_PRIMARY);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField amountField = new JTextField(15);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        amountField.setHorizontalAlignment(JTextField.CENTER);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(instructionLabel, BorderLayout.NORTH);
        centerPanel.add(amountField, BorderLayout.CENTER);

        inputPanel.add(titleLabel, BorderLayout.NORTH);
        inputPanel.add(centerPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this,
                inputPanel,
                "Custom Amount",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String input = amountField.getText().trim();
            if (!input.isEmpty()) {
                try {
                    double amount = Double.parseDouble(input);
                    handleCashPayment(amount);
                } catch (NumberFormatException e) {
                    showError("Invalid amount entered");
                }
            }
        }
    }

    private void handleCardPayment() {
        double total = controller.getTotal();

        JPanel confirmPanel = new JPanel(new BorderLayout(10, 15));
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("💳 Card Payment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_BLUE);

        JLabel amountLabel = new JLabel("Amount: $" + df.format(total));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        amountLabel.setForeground(TEXT_PRIMARY);
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        amountLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel questionLabel = new JLabel("Process card payment?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionLabel.setForeground(TEXT_SECONDARY);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(amountLabel, BorderLayout.CENTER);
        contentPanel.add(questionLabel, BorderLayout.SOUTH);

        confirmPanel.add(titleLabel, BorderLayout.NORTH);
        confirmPanel.add(contentPanel, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmPanel,
                "Confirm Card Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.completeTransaction("CREDIT", total);
            onPaymentComplete.run();
        }
    }

    private void handleCancelTransaction() {
        JPanel confirmPanel = new JPanel(new BorderLayout(10, 15));
        confirmPanel.setBackground(Color.WHITE);
        confirmPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("🗑️ Cancel Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_RED);

        JLabel warningLabel = new JLabel("<html><center>Are you sure you want to cancel this transaction?<br>This action cannot be undone.</center></html>");
        warningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        warningLabel.setForeground(TEXT_PRIMARY);
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        confirmPanel.add(titleLabel, BorderLayout.NORTH);
        confirmPanel.add(warningLabel, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this,
                confirmPanel,
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidTransaction();
            onBackRequested.run();
        }
    }

    // ==================== Public Methods ====================

    public void refresh() {
        // Clear and populate summary table
        summaryTableModel.setRowCount(0);
        List<TransactionItem> items = controller.getCurrentTransaction();

        for (TransactionItem item : items) {
            Object[] row = {
                    truncate(item.getProduct().getName(), 35),
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
            discountCard.setVisible(true);
        } else {
            discountCard.setVisible(false);
        }

        // Reset change panel
        changeDisplayPanel.setVisible(false);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}