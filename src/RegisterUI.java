import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

public class RegisterUI extends JFrame {
    // Constants
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
    private static final Color BACKGROUND_GRAY = new Color(220, 220, 220);
    private static final Color BUTTON_GREEN = new Color(119, 214, 135);
    private static final Color FLASH_GREEN = new Color(144, 238, 144, 100);
    private static final int TOTAL_PANEL_HEIGHT = 100;
    private static final int INPUT_PANEL_HEIGHT = 100;

    // Components
    private final RegisterController controller;
    private final DefaultTableModel tableModel;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    private JLabel totalLabel;
    private JTextField upcInput;
    private JTextField qtyInput;

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
        setTitle("Mock Register");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new ScannerKeyEventDispatcher());
    }

    private void setupComponents() {
        JPanel mainPanel = createMainPanel();
        mainPanel.add(createTotalPanel(), BorderLayout.NORTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createInputPanel(), BorderLayout.SOUTH);
        add(mainPanel);
        updateTotal();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private JPanel createTotalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setPreferredSize(new Dimension(0, TOTAL_PANEL_HEIGHT));

        JLabel totalTextLabel = new JLabel("TOTAL");
        totalTextLabel.setFont(new Font("Arial", Font.BOLD, 36));

        totalLabel = new JLabel("##,###.##");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 36));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(totalTextLabel, BorderLayout.WEST);
        panel.add(totalLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTable itemTable = createItemTable();
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
        return table;
    }

    private DefaultTableModel createTableModel() {
        String[] columns = {"UPC", "Description", "Price", "QTY", "Total per Item/s"};
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(0, INPUT_PANEL_HEIGHT));

        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("Manual UPC input:") {{
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
        upcInput.setPreferredSize(new Dimension(400, 40));
        upcInput.addActionListener(e -> handleAddItem());
        return upcInput;
    }

    private JTextField createQtyInput() {
        qtyInput = new JTextField(5);
        qtyInput.setFont(new Font("Arial", Font.PLAIN, 18));
        qtyInput.setPreferredSize(new Dimension(80, 40));
        qtyInput.addActionListener(e -> handleAddItem());
        qtyInput.setText("1");
        return qtyInput;
    }

    private JButton createAddButton() {
        JButton addButton = new JButton("ADD");
        addButton.setFont(new Font("Arial", Font.BOLD, 18));
        addButton.setPreferredSize(new Dimension(120, 40));
        addButton.setBackground(BUTTON_GREEN);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> handleAddItem());
        return addButton;
    }

    private void handleAddItem() {
        String upc = upcInput.getText().trim();
        if (upc.isEmpty()) {
            showError("Please enter a UPC code");
            return;
        }

        int qty = parseQuantity();
        if (qty <= 0) {
            return;
        }

        controller.addItem(upc, qty);
        clearInputs();
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

    public void addItemToTable(String upc, String description, double price, int qty, double total) {
        Object[] row = {upc, description, df.format(price), String.valueOf(qty), df.format(total)};
        tableModel.addRow(row);
        updateTotal();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void updateTotal() {
        double total = controller.getTotal();
        totalLabel.setText(total == 0 ? "##,###.##" : df.format(total));
    }

    public void clearTable() {
        tableModel.setRowCount(0);
        updateTotal();
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

    /*
     * This class listens for keyboard input that happens very quickly (like a barcode scanner).
     * * It works by tracking how much time passes between each key press:
     * - If keys are pressed very fast (under 100ms), it assumes it's a scanner and saves the text.
     * - If there is a long pause, it clears the saved text (assuming it was just a user typing slowly).
     * - When the 'Enter' key is detected, it sends the saved text to be processed.
     * - It returns 'true' to stop these keys from being typed into other text boxes on the screen.
     */
    private class ScannerKeyEventDispatcher implements KeyEventDispatcher {
        private static final long SCAN_TIMEOUT = 100;
        private final StringBuilder scanBuffer = new StringBuilder();
        private long lastKeyTime = 0;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }

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
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '-' || c == '_') {
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