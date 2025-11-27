import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Revamped Transaction Panel with modern UI design.
 * Features improved visual hierarchy, card-based layouts, and better usability.
 */
public class TransactionPanel extends JPanel {

    // Modern Color Scheme
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
    private static final Color TABLE_ALT_ROW = new Color(249, 250, 251);

    // Components
    private final RegisterController controller;
    private final Runnable onPaymentRequested;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    // UI Elements
    private JLabel subtotalLabel;
    private JLabel discountAmountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JLabel transactionStatusLabel;
    private JPanel discountCard;
    private JTextField upcInput;
    private JTextField qtyInput;
    private JTable itemTable;
    private JPanel discountBadge;

    public TransactionPanel(RegisterController controller, Runnable onPaymentRequested) {
        this.controller = controller;
        this.onPaymentRequested = onPaymentRequested;
        this.tableModel = createTableModel();

        setLayout(new BorderLayout(15, 15));
        setBackground(PRIMARY_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        add(createHeaderSection(), BorderLayout.NORTH);
        add(createMainSection(), BorderLayout.CENTER);
        add(createFooterSection(), BorderLayout.SOUTH);
    }

    // ==================== Header Section ====================

    private JPanel createHeaderSection() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PRIMARY_BG);

        // Status and Title Row
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(PRIMARY_BG);

        JLabel titleLabel = new JLabel("Transaction");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);

        transactionStatusLabel = new JLabel("");
        transactionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        transactionStatusLabel.setForeground(ACCENT_BLUE);

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(transactionStatusLabel, BorderLayout.EAST);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(createTotalsSection(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTotalsSection() {
        JPanel container = new JPanel(new GridLayout(1, 4, 15, 0));
        container.setBackground(PRIMARY_BG);

        // Subtotal Card
        container.add(createTotalCard("Subtotal", "$0.00", TEXT_SECONDARY, false));

        // Tax Card
        container.add(createTotalCard("Tax (7%)", "$0.00", TEXT_SECONDARY, false));

        // Discount Card (hidden initially)
        discountCard = createTotalCard("Discount", "-$0.00", DISCOUNT_GREEN, false);
        discountCard.setVisible(false);
        container.add(discountCard);

        // Total Card (emphasized)
        container.add(createTotalCard("Total", "$0.00", ACCENT_GREEN, true));

        return container;
    }

    private JPanel createTotalCard(String label, String value, Color valueColor, boolean emphasized) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Label
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, emphasized ? 16 : 14));
        labelComp.setForeground(TEXT_SECONDARY);

        // Value
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, emphasized ? 32 : 24));
        valueComp.setForeground(valueColor);

        // Store references
        if (label.equals("Subtotal")) subtotalLabel = valueComp;
        else if (label.equals("Discount")) discountAmountLabel = valueComp;
        else if (label.contains("Tax")) taxLabel = valueComp;
        else if (label.equals("Total")) totalLabel = valueComp;

        card.add(labelComp, BorderLayout.NORTH);
        card.add(valueComp, BorderLayout.CENTER);

        return card;
    }

    // ==================== Main Section ====================

    private JPanel createMainSection() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PRIMARY_BG);

        // Left: Items Table
        panel.add(createItemsSection(), BorderLayout.CENTER);

        // Right: Actions and Quick Keys
        JPanel rightPanel = new JPanel(new BorderLayout(15, 15));
        rightPanel.setBackground(PRIMARY_BG);
        rightPanel.setPreferredSize(new Dimension(380, 0));

        rightPanel.add(createActionsSection(), BorderLayout.NORTH);
        rightPanel.add(createQuickKeysSection(), BorderLayout.CENTER);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createItemsSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Header with badge
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_BG);

        JLabel headerLabel = new JLabel("Items");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(TEXT_PRIMARY);

        // Discount badge (hidden initially)
        discountBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        discountBadge.setBackground(CARD_BG);
        discountBadge.setVisible(false);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(discountBadge, BorderLayout.EAST);

        // Table
        itemTable = createStyledTable();
        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable() {
        JTable table = new JTable(tableModel);

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

        // Clean cell renderer without alternating colors
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
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        return table;
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {"UPC", "Description", "Price", "Qty", "Total"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createActionsSection() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(PRIMARY_BG);

        // Group 1: Item Actions
        JPanel itemActions = new JPanel(new GridLayout(1, 2, 10, 0));
        itemActions.setBackground(PRIMARY_BG);
        itemActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        itemActions.add(createActionButton("Void Item", ACCENT_RED, e -> handleVoidItem()));
        itemActions.add(createActionButton("Change Qty", ACCENT_BLUE, e -> handleQuantityChange()));
        container.add(itemActions);
        container.add(Box.createVerticalStrut(10));

        // Group 2: Transaction Actions (Suspend/Resume)
        JPanel transactionActions = new JPanel(new GridLayout(1, 2, 10, 0));
        transactionActions.setBackground(PRIMARY_BG);
        transactionActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        transactionActions.add(createActionButton("Suspend", ACCENT_ORANGE, e -> handleSuspendTransaction()));
        transactionActions.add(createActionButton("Resume", ACCENT_ORANGE, e -> handleResumeTransaction()));
        container.add(transactionActions);
        container.add(Box.createVerticalStrut(10));

        // Group 3: Major Actions (Void Trans / Pay Now)
        JPanel majorActions = new JPanel(new GridLayout(1, 2, 10, 0));
        majorActions.setBackground(PRIMARY_BG);
        majorActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        majorActions.add(createActionButton("Void Trans", ACCENT_RED, e -> handleVoidTransaction()));

        JButton payButton = createActionButton("PAY NOW", ACCENT_GREEN, e -> handlePayment());
        payButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        majorActions.add(payButton);
        container.add(majorActions);
        container.add(Box.createVerticalStrut(10));

        // Group 4: History (Full Width)
        JPanel historyPanel = new JPanel(new GridLayout(1, 1));
        historyPanel.setBackground(PRIMARY_BG);
        historyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        historyPanel.add(createActionButton("Transaction History", ACCENT_BLUE, e -> handleShowHistory()));
        container.add(historyPanel);

        return container;
    }

    private JButton createActionButton(String text, Color color, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 50));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        btn.addActionListener(listener);
        return btn;
    }

    private JPanel createQuickKeysSection() {
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(CARD_BG);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel headerLabel = new JLabel("Quick Keys");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setForeground(TEXT_PRIMARY);

        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        gridPanel.setBackground(CARD_BG);

        addQuickKey(gridPanel, "Polar Pop 42oz", "041594904794", 8.91);
        addQuickKey(gridPanel, "Hot Dog", "999999955678", 2.69);
        addQuickKey(gridPanel, "Large Coffee", "999991218948", 2.19);
        addQuickKey(gridPanel, "Medium Polar", "999999937551", 0.89);
        addQuickKey(gridPanel, "Marlboro Gold", "028200003843", 8.47);
        addQuickKey(gridPanel, "Monster Energy", "070847811169", 3.29);
        addQuickKey(gridPanel, "Mt Dew 20oz", "012000001314", 9.96);
        addQuickKey(gridPanel, "Donut", "049000000443", 2.49);

        container.add(headerLabel, BorderLayout.NORTH);
        container.add(gridPanel, BorderLayout.CENTER);

        return container;
    }

    private void addQuickKey(JPanel panel, String name, String upc, double price) {
        JButton btn = new JButton("<html><div style='text-align: center;'><b>" +
                truncate(name, 12) + "</b><br><span style='color: #10b981;'>$" +
                df.format(price) + "</span></div></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(CARD_BG);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(TABLE_ALT_ROW);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(CARD_BG);
            }
        });

        btn.addActionListener(e -> controller.addItem(upc, 1));
        panel.add(btn);
    }

    // ==================== Footer Section ====================

    private JPanel createFooterSection() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(PRIMARY_BG);

        // Input section
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        inputPanel.setBackground(PRIMARY_BG);

        // UPC Input
        JPanel upcGroup = createInputGroup("Product Code (UPC)");
        upcInput = createStyledInput(25);
        upcInput.addActionListener(e -> handleAddItem());
        upcGroup.add(upcInput);
        inputPanel.add(upcGroup);

        // Quantity Input
        JPanel qtyGroup = createInputGroup("Quantity");
        qtyInput = createStyledInput(5);
        qtyInput.setText("1");
        qtyInput.addActionListener(e -> handleAddItem());
        qtyGroup.add(qtyInput);
        inputPanel.add(qtyGroup);

        // Add Button - aligned to bottom of input groups
        JPanel addButtonWrapper = new JPanel(new BorderLayout());
        addButtonWrapper.setBackground(PRIMARY_BG);
        addButtonWrapper.add(Box.createVerticalStrut(22), BorderLayout.NORTH); // Offset for label height

        JButton addButton = new JButton("Add Item");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setBackground(ACCENT_GREEN);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setPreferredSize(new Dimension(120, 45));
        addButton.addActionListener(e -> handleAddItem());
        addButtonWrapper.add(addButton, BorderLayout.CENTER);

        inputPanel.add(addButtonWrapper);

        panel.add(inputPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputGroup(String label) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(PRIMARY_BG);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelComp.setForeground(TEXT_SECONDARY);

        panel.add(labelComp, BorderLayout.NORTH);
        return panel;
    }

    private JTextField createStyledInput(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        return field;
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

        // Get item details for confirmation
        String upc = (String) tableModel.getValueAt(selectedRow, 0);
        String desc = (String) tableModel.getValueAt(selectedRow, 1);
        String qty = (String) tableModel.getValueAt(selectedRow, 3);
        String total = (String) tableModel.getValueAt(selectedRow, 4);

        // Create custom confirmation panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel warningLabel = new JLabel("⚠️ Void Item", SwingConstants.CENTER);
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        warningLabel.setForeground(ACCENT_RED);

        JPanel detailsPanel = new JPanel(new GridLayout(4, 1, 5, 8));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        detailsPanel.add(createDetailLabel("UPC: " + upc));
        detailsPanel.add(createDetailLabel("Item: " + desc));
        detailsPanel.add(createDetailLabel("Quantity: " + qty));
        detailsPanel.add(createDetailLabel("Total: " + total));

        JLabel questionLabel = new JLabel("Are you sure you want to void this item?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionLabel.setForeground(TEXT_SECONDARY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(warningLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(questionLabel, BorderLayout.SOUTH);

        int confirm = JOptionPane.showConfirmDialog(this, panel,
                "Confirm Void Item",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidItem(selectedRow);
            showToast("Item voided successfully", ACCENT_GREEN);
        }
    }

    /**
     * Shows a toast notification at the bottom-right of the panel.
     */
    private void showToast(String message, Color backgroundColor) {
        JWindow toast = new JWindow();
        toast.setAlwaysOnTop(true);

        JPanel toastPanel = new JPanel(new BorderLayout(10, 10));
        toastPanel.setBackground(backgroundColor);
        toastPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 2, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel icon = new JLabel("✓");
        icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        icon.setForeground(Color.WHITE);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        messageLabel.setForeground(Color.WHITE);

        toastPanel.add(icon, BorderLayout.WEST);
        toastPanel.add(messageLabel, BorderLayout.CENTER);

        toast.add(toastPanel);
        toast.pack();

        // Position at bottom-right of this panel
        Point panelLocation = this.getLocationOnScreen();
        int x = panelLocation.x + this.getWidth() - toast.getWidth() - 30;
        int y = panelLocation.y + this.getHeight() - toast.getHeight() - 30;
        toast.setLocation(x, y);

        toast.setOpacity(0.95f);
        toast.setVisible(true);

        // Auto-hide after 2.5 seconds
        Timer timer = new Timer(2500, e -> {
            toast.setVisible(false);
            toast.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private void handleQuantityChange() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an item");
            return;
        }

        // Get current item details
        String upc = (String) tableModel.getValueAt(selectedRow, 0);
        String desc = (String) tableModel.getValueAt(selectedRow, 1);
        String currentQty = (String) tableModel.getValueAt(selectedRow, 3);

        // Create custom quantity change panel
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("📝 Change Quantity", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_BLUE);

        // Item details panel
        JPanel detailsPanel = new JPanel(new GridLayout(3, 1, 5, 8));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        detailsPanel.add(createDetailLabel("UPC: " + upc));
        detailsPanel.add(createDetailLabel("Item: " + desc));
        detailsPanel.add(createDetailLabel("Current Quantity: " + currentQty));

        // Input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBackground(Color.WHITE);

        JLabel inputLabel = new JLabel("New Quantity:");
        inputLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inputLabel.setForeground(TEXT_PRIMARY);

        JTextField qtyField = new JTextField(10);
        qtyField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        qtyField.setText(currentQty);
        qtyField.setPreferredSize(new Dimension(120, 35));
        qtyField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        qtyField.selectAll(); // Pre-select text for easy overwrite

        inputPanel.add(inputLabel);
        inputPanel.add(qtyField);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Change Quantity",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String input = qtyField.getText().trim();
            if (!input.isEmpty()) {
                try {
                    int newQty = Integer.parseInt(input);
                    if (newQty > 0) {
                        int oldQty = Integer.parseInt(currentQty);
                        controller.changeQuantity(selectedRow, newQty);

                        // Show success toast with quantity change info
                        String message = "Quantity changed: " + oldQty + " → " + newQty;
                        showToast(message, ACCENT_BLUE);
                    } else {
                        showError("Quantity must be greater than 0");
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid quantity entered");
                }
            }
        }
    }

    private void handleVoidTransaction() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No transaction to void");
            return;
        }

        // Create custom void transaction panel
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("🗑️ Void Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_RED);

        // Transaction summary with proper row count
        int itemCount = controller.getCurrentTransaction().size();
        double subtotal = controller.getSubtotal();
        double discount = controller.getDiscountAmount();
        double total = controller.getTotal();

        int rowCount = discount > 0 ? 4 : 3;
        JPanel summaryPanel = new JPanel(new GridLayout(rowCount, 1, 5, 8));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        summaryPanel.add(createDetailLabel("Items in transaction: " + itemCount));
        summaryPanel.add(createDetailLabel("Subtotal: $" + df.format(subtotal)));
        if (discount > 0) {
            JLabel discountLabel = createDetailLabel("Discount: -$" + df.format(discount));
            discountLabel.setForeground(DISCOUNT_GREEN);
            summaryPanel.add(discountLabel);
        }
        summaryPanel.add(createDetailLabel("Total amount: $" + df.format(total)));

        // Warning message
        JPanel warningPanel = new JPanel(new BorderLayout());
        warningPanel.setBackground(new Color(254, 242, 242)); // Light red background
        warningPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_RED, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel warningLabel = new JLabel("<html><b>⚠️ Warning:</b> This action cannot be undone. The entire transaction will be voided and removed.</html>");
        warningLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        warningLabel.setForeground(new Color(127, 29, 29)); // Dark red text
        warningPanel.add(warningLabel);

        JLabel questionLabel = new JLabel("Are you sure you want to void this transaction?");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionLabel.setForeground(TEXT_PRIMARY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(summaryPanel, BorderLayout.NORTH);
        contentPanel.add(warningPanel, BorderLayout.CENTER);
        contentPanel.add(questionLabel, BorderLayout.SOUTH);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        int confirm = JOptionPane.showConfirmDialog(this, panel,
                "Confirm Void Transaction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidTransaction();
            showToast("Transaction voided successfully", ACCENT_RED);
        }
    }

    private void handleSuspendTransaction() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No transaction to suspend");
            return;
        }

        // Create custom confirmation panel
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("⏸️ Suspend Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ACCENT_ORANGE);

        // Transaction summary
        JPanel summaryPanel = new JPanel(new GridLayout(3, 1, 5, 8));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        int itemCount = controller.getCurrentTransaction().size();
        double total = controller.getTotal();

        summaryPanel.add(createDetailLabel("Items in transaction: " + itemCount));
        summaryPanel.add(createDetailLabel("Total amount: $" + df.format(total)));
        summaryPanel.add(createDetailLabel("Status: Transaction will be saved and can be resumed later"));

        JLabel questionLabel = new JLabel("Do you want to suspend this transaction?");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionLabel.setForeground(TEXT_SECONDARY);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(summaryPanel, BorderLayout.CENTER);
        panel.add(questionLabel, BorderLayout.SOUTH);

        int confirm = JOptionPane.showConfirmDialog(this, panel,
                "Confirm Suspend Transaction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.suspendTransaction();

            // Show success toast notification
            showToast("Transaction suspended successfully", ACCENT_ORANGE);
        }
    }

    private void handleResumeTransaction() {
        controller.resumeTransaction();
    }

    private void handleShowHistory() {
        controller.showTransactionHistory();
    }

    private void handlePayment() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No items in transaction");
            return;
        }
        onPaymentRequested.run();
    }

    // ==================== Public UI Update Methods ====================

    public void addItemToTable(String upc, String desc, double price, int qty, double total) {
        Object[] row = {upc, truncate(desc, 45), "$" + df.format(price),
                String.valueOf(qty), "$" + df.format(total)};
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
            discountCard.setVisible(true);
        } else {
            discountCard.setVisible(false);
        }
    }

    public void setDiscountStatus(List<String> appliedDiscounts) {
        discountBadge.removeAll();

        if (appliedDiscounts != null && !appliedDiscounts.isEmpty()) {
            for (String discount : appliedDiscounts) {
                JLabel badge = new JLabel("✓ " + discount);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setForeground(Color.WHITE);
                badge.setBackground(DISCOUNT_GREEN);
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                discountBadge.add(badge);
            }
            discountBadge.setVisible(true);
        } else {
            discountBadge.setVisible(false);
        }

        discountBadge.revalidate();
        discountBadge.repaint();
    }

    public void setTransactionStatus(String status) {
        transactionStatusLabel.setText(status);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JTextField getUpcInput() {
        return upcInput;
    }

    public JTextField getQtyInput() {
        return qtyInput;
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

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}