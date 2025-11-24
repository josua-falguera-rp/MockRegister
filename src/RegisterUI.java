import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class RegisterUI extends JFrame {
    // Constants
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 1000;
    private static final Color BACKGROUND_GRAY = new Color(220, 220, 220);
    private static final Color BUTTON_GREEN = new Color(119, 214, 135);
    private static final Color BUTTON_RED = new Color(220, 100, 100);
    private static final Color BUTTON_BLUE = new Color(100, 150, 220);
    private static final Color BUTTON_YELLOW = new Color(255, 200, 100);
    private static final Color FLASH_GREEN = new Color(144, 238, 144, 100);

    // Components
    private final RegisterController controller;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");

    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JLabel transactionStatusLabel;
    private JTextField upcInput;
    private JTextField qtyInput;
    private JTable itemTable;
    private JPanel quickKeysPanel;

    public RegisterUI(RegisterController controller) {
        this.controller = controller;
        this.tableModel = createTableModel();
        initializeUI();
    }

    private void initializeUI() {
        setupFrame();
        setupGlobalKeyListener();
        setupComponents();
    }

    private void setupFrame() {
        setTitle("Mock Register - Enhanced with Transaction Management");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new ScannerKeyEventDispatcher());
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        updateTotals(0, 0, 0);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // Add transaction status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        transactionStatusLabel = new JLabel("");
        transactionStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        transactionStatusLabel.setForeground(new Color(0, 100, 0));
        statusPanel.add(transactionStatusLabel, BorderLayout.WEST);

        panel.add(statusPanel, BorderLayout.NORTH);
        panel.add(createTotalsPanel(), BorderLayout.CENTER);
        panel.add(createQuickKeysPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBackground(BACKGROUND_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 150));

        subtotalLabel = createTotalLabel("SUBTOTAL");
        taxLabel = createTotalLabel("TAX (7%)");
        totalLabel = createTotalLabel("TOTAL");

        panel.add(createTotalRow("SUBTOTAL:", subtotalLabel));
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

    private JLabel createTotalLabel(String initialText) {
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

        // Add some default quick keys
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
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 10));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createActionButton("Void Item", BUTTON_RED, e -> handleVoidItem()));
        panel.add(createActionButton("Qty Change", BUTTON_BLUE, e -> handleQuantityChange()));
        panel.add(createActionButton("Void Trans", BUTTON_RED, e -> handleVoidTransaction()));
        panel.add(createActionButton("Suspend", BUTTON_YELLOW, e -> handleSuspendTransaction()));
        panel.add(createActionButton("Resume", BUTTON_YELLOW, e -> handleResumeTransaction()));
        panel.add(createActionButton("History", BUTTON_BLUE, e -> handleShowHistory()));
        panel.add(createPaymentButton("Cash", "CASH"));
        panel.add(createPaymentButton("Card", "CREDIT"));

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

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("UPC:") {{
            setFont(new Font("Arial", Font.PLAIN, 18));
        }});
        panel.add(createUpcInput());

        panel.add(new JLabel("QTY:") {{
            setFont(new Font("Arial", Font.PLAIN, 18));
        }});
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

    private JButton createPaymentButton(String text, String paymentType) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(120, 45));
        btn.setBackground(BUTTON_GREEN);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> handlePayment(paymentType));
        return btn;
    }

    // Event Handlers

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
                "Void selected item?", "Confirm Void",
                JOptionPane.YES_NO_OPTION);

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
                "Void entire transaction?",
                "Confirm Void Transaction",
                JOptionPane.YES_NO_OPTION);

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

    private void handleShowHistory() {
        controller.showTransactionHistory();
    }

    private void handleExactDollar() {
        double total = controller.getTotal();
        if (total <= 0) {
            showError("No items in transaction");
            return;
        }

        controller.completeTransaction("CASH", total);
    }

    private void handleNextDollar() {
        double total = controller.getTotal();
        if (total <= 0) {
            showError("No items in transaction");
            return;
        }

        double nextDollar = Math.ceil(total);
        controller.completeTransaction("CASH", nextDollar);
    }

    private void handlePayment(String paymentType) {
        double total = controller.getTotal();
        if (total <= 0) {
            showError("No items in transaction");
            return;
        }

        if (paymentType.equals("CASH")) {
            // Create custom dialog for cash payment with quick options
            JDialog cashDialog = new JDialog(this, "Cash Payment", true);
            cashDialog.setLayout(new BorderLayout());
            cashDialog.setSize(400, 300);
            cashDialog.setLocationRelativeTo(this);

            // Top panel with total display
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(BACKGROUND_GRAY);
            topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel totalDisplayLabel = new JLabel("Total: $" + df.format(total));
            totalDisplayLabel.setFont(new Font("Arial", Font.BOLD, 24));
            totalDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(totalDisplayLabel, BorderLayout.CENTER);

            // Middle panel with quick payment options
            JPanel quickOptionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
            quickOptionsPanel.setBackground(Color.WHITE);
            quickOptionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

            JButton exactButton = new JButton("Exact Amount - $" + df.format(total));
            exactButton.setFont(new Font("Arial", Font.BOLD, 18));
            exactButton.setBackground(BUTTON_GREEN);
            exactButton.setFocusPainted(false);
            exactButton.addActionListener(e -> {
                cashDialog.dispose();
                controller.completeTransaction("CASH", total);
            });

            double nextDollar = Math.ceil(total);
            JButton nextDollarButton = new JButton("Next Dollar - $" + df.format(nextDollar));
            nextDollarButton.setFont(new Font("Arial", Font.BOLD, 18));
            nextDollarButton.setBackground(BUTTON_GREEN);
            nextDollarButton.setFocusPainted(false);
            nextDollarButton.addActionListener(e -> {
                cashDialog.dispose();
                controller.completeTransaction("CASH", nextDollar);
            });

            quickOptionsPanel.add(exactButton);
            quickOptionsPanel.add(nextDollarButton);

            // Bottom panel with custom amount option
            JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            customPanel.setBackground(Color.WHITE);
            customPanel.setBorder(BorderFactory.createTitledBorder("Custom Amount"));

            JLabel customLabel = new JLabel("Enter amount:");
            customLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JTextField customAmount = new JTextField(10);
            customAmount.setFont(new Font("Arial", Font.PLAIN, 16));
            customAmount.setPreferredSize(new Dimension(120, 35));

            JButton customButton = new JButton("Pay");
            customButton.setFont(new Font("Arial", Font.BOLD, 16));
            customButton.setBackground(BUTTON_BLUE);
            customButton.setFocusPainted(false);
            customButton.addActionListener(e -> {
                try {
                    double tendered = Double.parseDouble(customAmount.getText().trim());
                    if (tendered >= total) {
                        cashDialog.dispose();
                        controller.completeTransaction("CASH", tendered);
                    } else {
                        JOptionPane.showMessageDialog(cashDialog,
                                "Insufficient amount. Need at least $" + df.format(total),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(cashDialog,
                            "Invalid amount entered", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Allow Enter key to submit custom amount
            customAmount.addActionListener(e -> customButton.doClick());

            customPanel.add(customLabel);
            customPanel.add(customAmount);
            customPanel.add(customButton);

            // Cancel button panel
            JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            cancelPanel.setBackground(Color.WHITE);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
            cancelButton.setBackground(BUTTON_RED);
            cancelButton.setFocusPainted(false);
            cancelButton.addActionListener(e -> cashDialog.dispose());
            cancelPanel.add(cancelButton);

            // Combine bottom panels
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(customPanel, BorderLayout.CENTER);
            bottomPanel.add(cancelPanel, BorderLayout.SOUTH);

            cashDialog.add(topPanel, BorderLayout.NORTH);
            cashDialog.add(quickOptionsPanel, BorderLayout.CENTER);
            cashDialog.add(bottomPanel, BorderLayout.SOUTH);

            cashDialog.setVisible(true);

        } else if (paymentType.equals("CREDIT")) {
            // For credit card, process as exact amount automatically
            controller.completeTransaction("CREDIT", total);
        }
    }

    // UI Update Methods

    public void addItemToTable(String upc, String description, double price, int qty, double total) {
        Object[] row = {upc, truncate(description, 40), df.format(price),
                String.valueOf(qty), df.format(total)};
        tableModel.addRow(row);
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void updateTotals(double subtotal, double tax, double total) {
        subtotalLabel.setText("$" + df.format(subtotal));
        taxLabel.setText("$" + df.format(tax));
        totalLabel.setText("$" + df.format(total));
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

    public void showPaymentComplete(double total, double tendered, double change) {
        String message = String.format(
                "Payment Complete\n\nTotal: $%s\nTendered: $%s\nChange: $%s",
                df.format(total), df.format(tendered), df.format(change)
        );
        JOptionPane.showMessageDialog(this, message, "Payment Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public int confirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public String showSelectionDialog(String message, String title, String[] options) {
        return (String) JOptionPane.showInputDialog(this, message, title,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    public void showTransactionHistory(List<Map<String, Object>> history) {
        String[] columns = {"ID", "Date", "Total", "Payment", "Status"};
        Object[][] data = new Object[history.size()][5];

        for (int i = 0; i < history.size(); i++) {
            Map<String, Object> trans = history.get(i);
            data[i][0] = trans.get("id");
            data[i][1] = dateFormat.format(trans.get("date"));
            data[i][2] = "$" + df.format(trans.get("total"));
            data[i][3] = trans.get("payment_type") != null ? trans.get("payment_type") : "-";

            String status = "";
            if ((Boolean) trans.get("is_voided")) status = "VOIDED";
            else if ((Boolean) trans.get("is_suspended") && !(Boolean) trans.get("is_resumed")) status = "SUSPENDED";
            else if ((Boolean) trans.get("is_completed")) status = "COMPLETED";
            else status = "IN PROGRESS";

            data[i][4] = status;
        }

        JTable historyTable = new JTable(data, columns);
        historyTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Transaction History", JOptionPane.PLAIN_MESSAGE);
    }

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

    private void flashUpcField(String upc) {
        upcInput.setText(upc);
        upcInput.setBackground(FLASH_GREEN);

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

    // Scanner Key Event Dispatcher

    private class ScannerKeyEventDispatcher implements KeyEventDispatcher {
        private static final long SCAN_TIMEOUT = 100;
        private final StringBuilder scanBuffer = new StringBuilder();
        private long lastKeyTime = 0;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;

            long currentTime = System.currentTimeMillis();

            if (isTimedOut(currentTime)) {
                scanBuffer.setLength(0);
            }

            lastKeyTime = currentTime;

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                return handleEnterKey();
            }

            return bufferCharacter(e.getKeyChar());
        }

        private boolean isTimedOut(long currentTime) {
            return currentTime - lastKeyTime > SCAN_TIMEOUT && !scanBuffer.isEmpty();
        }

        private boolean handleEnterKey() {
            if (!scanBuffer.isEmpty()) {
                String scannedUPC = scanBuffer.toString().trim();
                scanBuffer.setLength(0);
                SwingUtilities.invokeLater(() -> processScan(scannedUPC));
                return true;
            }
            return false;
        }

        private boolean bufferCharacter(char c) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) ||
                    c == '-' || c == '_') {
                scanBuffer.append(c);
                return true;
            }
            return false;
        }

        private void processScan(String upc) {
            controller.addItem(upc, 1);
            flashUpcField(upc);
            qtyInput.setText("1");
        }
    }
}