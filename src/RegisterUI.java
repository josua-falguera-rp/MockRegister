import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class RegisterUI extends JFrame {
    // Constants
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 1000;
    private static final Color BACKGROUND_GRAY = new Color(220, 220, 220);
    private static final Color BUTTON_GREEN = new Color(119, 214, 135);
    private static final Color BUTTON_RED = new Color(220, 100, 100);
    private static final Color BUTTON_BLUE = new Color(100, 150, 220);
    private static final Color FLASH_GREEN = new Color(144, 238, 144, 100);

    // Components
    private final RegisterController controller;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
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
        setTitle("Mock Register - Enhanced");
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
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 10));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createActionButton("Void Item", BUTTON_RED, e -> handleVoidItem()));
        panel.add(createActionButton("Qty Change", BUTTON_BLUE, e -> handleQuantityChange()));
        panel.add(createActionButton("Void Trans", BUTTON_RED, e -> handleVoidTransaction()));
        panel.add(createActionButton("Suspend", BUTTON_BLUE, e -> handleSuspendTransaction()));
        panel.add(createActionButton("Exact $", BUTTON_GREEN, e -> handleExactDollar()));
        panel.add(createActionButton("Next $", BUTTON_GREEN, e -> handleNextDollar()));

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
        panel.add(createPaymentButton("Cash", "CASH"));
        panel.add(createPaymentButton("Card", "CREDIT"));

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
                "Void entire transaction?", "Confirm Void Transaction",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.voidTransaction();
            JOptionPane.showMessageDialog(this, "Transaction voided");
        }
    }

    private void handleSuspendTransaction() {
        if (controller.getCurrentTransaction().isEmpty()) {
            showError("No transaction to suspend");
            return;
        }

        controller.suspendTransaction();
        JOptionPane.showMessageDialog(this, "Transaction suspended");
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

        String input = JOptionPane.showInputDialog(this,
                "Total: $" + df.format(total) + "\nEnter amount tendered:");

        if (input != null && !input.trim().isEmpty()) {
            try {
                double tendered = Double.parseDouble(input.trim());
                controller.completeTransaction(paymentType, tendered);
            } catch (NumberFormatException e) {
                showError("Invalid amount");
            }
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

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showPaymentComplete(double total, double tendered, double change) {
        String message = String.format(
                "Payment Complete\n\nTotal: $%s\nTendered: $%s\nChange: $%s",
                df.format(total), df.format(tendered), df.format(change)
        );
        JOptionPane.showMessageDialog(this, message, "Payment Complete",
                JOptionPane.INFORMATION_MESSAGE);
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