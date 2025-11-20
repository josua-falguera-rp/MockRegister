import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class RegisterUI extends JFrame {
    private RegisterController controller;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private JTextField upcInput;
    private JTextField qtyInput;
    private JButton addButton;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public RegisterUI(RegisterController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Mock Register - Scanner Ready");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Enable global key listener for scanner input
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new ScannerKeyEventDispatcher());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Total panel at top
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(220, 220, 220));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        totalPanel.setPreferredSize(new Dimension(0, 100));

        JLabel totalTextLabel = new JLabel("TOTAL");
        totalTextLabel.setFont(new Font("Arial", Font.BOLD, 36));

        totalLabel = new JLabel("##,###.##");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 36));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(220, 220, 220));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"UPC", "Description", "Price", "QTY", "Total per Item/s"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setFont(new Font("Arial", Font.PLAIN, 16));
        itemTable.setRowHeight(30);
        itemTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        itemTable.getTableHeader().setBackground(new Color(200, 200, 200));
        itemTable.setBackground(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Input panel at bottom
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setPreferredSize(new Dimension(0, 100));

        JLabel upcLabel = new JLabel("Manual UPC input:");
        upcLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        upcInput = new JTextField(20);
        upcInput.setFont(new Font("Arial", Font.PLAIN, 18));
        upcInput.setPreferredSize(new Dimension(400, 40));

        JLabel qtyLabel = new JLabel("QTY:");
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        qtyInput = new JTextField(5);
        qtyInput.setFont(new Font("Arial", Font.PLAIN, 18));
        qtyInput.setPreferredSize(new Dimension(80, 40));
        qtyInput.setText("1");

        addButton = new JButton("ADD");
        addButton.setFont(new Font("Arial", Font.BOLD, 18));
        addButton.setPreferredSize(new Dimension(120, 40));
        addButton.setBackground(new Color(119, 214, 135));
        addButton.setFocusPainted(false);

        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(upcLabel);
        inputPanel.add(upcInput);
        inputPanel.add(qtyLabel);
        inputPanel.add(qtyInput);
        inputPanel.add(addButton);

        // Add all panels to main panel
        mainPanel.add(totalPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Event listeners
        addButton.addActionListener(e -> handleAddItem());
        upcInput.addActionListener(e -> handleAddItem());

        updateTotal();
    }

    private void handleAddItem() {
        String upc = upcInput.getText().trim();
        String qtyText = qtyInput.getText().trim();

        if (upc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a UPC code", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        controller.addItem(upc, qty);

        // Clear inputs
        upcInput.setText("");
        qtyInput.setText("1");
        upcInput.requestFocus();
    }

    public void addItemToTable(String upc, String description, double price, int qty, double total) {
        Object[] row = {
                upc,
                description,
                df.format(price),
                String.valueOf(qty),
                df.format(total)
        };
        tableModel.addRow(row);
        updateTotal();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateTotal() {
        double total = controller.getTotal();
        if (total == 0) {
            totalLabel.setText("##,###.##");
        } else {
            totalLabel.setText(df.format(total));
        }
    }

    public void clearTable() {
        tableModel.setRowCount(0);
        updateTotal();
    }

    // Scanner input handler
    private class ScannerKeyEventDispatcher implements KeyEventDispatcher {
        private StringBuilder scanBuffer = new StringBuilder();
        private long lastKeyTime = 0;
        private static final long SCAN_TIMEOUT = 100; // milliseconds between keystrokes for scanner

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            // Only process key pressed events
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }

            long currentTime = System.currentTimeMillis();

            // If too much time has passed, reset buffer (it's manual typing, not scanner)
            if (currentTime - lastKeyTime > SCAN_TIMEOUT && !scanBuffer.isEmpty()) {
                scanBuffer.setLength(0);
            }

            lastKeyTime = currentTime;

            // Check if Enter key was pressed
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (!scanBuffer.isEmpty()) {
                    // Scanner input detected
                    String scannedUPC = scanBuffer.toString().trim();
                    scanBuffer.setLength(0);

                    // Process the scanned barcode
                    SwingUtilities.invokeLater(() -> processScan(scannedUPC));

                    return true; // Consume the event
                }
                return false;
            }

            // Build up the scan buffer with alphanumeric characters
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '-' || c == '_') {
                scanBuffer.append(c);
                return true; // Consume the event
            }

            return false;
        }

        private void processScan(String upc) {
            // Get quantity from input field, default to 1
            int qty = 1;
            try {
                String qtyText = qtyInput.getText().trim();
                if (!qtyText.isEmpty()) {
                    qty = Integer.parseInt(qtyText);
                }
            } catch (NumberFormatException e) {
                // Use default quantity of 1
            }

            // Add the item
            controller.addItem(upc, qty);

            // Visual feedback
            upcInput.setText(upc);
            upcInput.setBackground(new Color(144, 238, 144, 100));

            // Reset background after a moment
            Timer timer = new Timer(200, evt -> {
                upcInput.setText("");
                upcInput.setBackground(Color.WHITE);
            });
            timer.setRepeats(false);
            timer.start();

            // Reset quantity to 1
            qtyInput.setText("1");
        }
    }
}