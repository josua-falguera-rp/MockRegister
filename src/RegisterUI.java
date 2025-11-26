import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Main UI frame for the register application.
 * Uses CardLayout to switch between Transaction and Payment views.
 */
public class RegisterUI extends JFrame {

    // View identifiers
    private static final String TRANSACTION_VIEW = "TRANSACTION";
    private static final String PAYMENT_VIEW = "PAYMENT";

    // Window constants
    private static final int WINDOW_WIDTH = 1600;
    private static final int WINDOW_HEIGHT = 1000;

    // Components
    private final RegisterController controller;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");

    // Layout components
    private CardLayout cardLayout;
    private JPanel mainCardPanel;

    // View panels
    private TransactionPanel transactionPanel;
    private PaymentPanel paymentPanel;

    public RegisterUI(RegisterController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setupFrame();
        setupCardLayout();
        setupGlobalKeyListener();
    }

    private void setupFrame() {
        setTitle("Mock Register");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupCardLayout() {
        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);

        // Create transaction panel with callback to switch to payment
        transactionPanel = new TransactionPanel(controller, this::showPaymentView);

        // Create payment panel with callbacks
        paymentPanel = new PaymentPanel(
                controller,
                this::showTransactionView,    // Back callback
                this::onPaymentComplete       // Payment complete callback
        );

        // Add panels to card layout
        mainCardPanel.add(transactionPanel, TRANSACTION_VIEW);
        mainCardPanel.add(paymentPanel, PAYMENT_VIEW);

        add(mainCardPanel);

        // Start with transaction view
        showTransactionView();
    }

    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new ScannerKeyEventDispatcher());
    }

    // ==================== View Navigation ====================

    /**
     * Switches to the transaction view.
     */
    public void showTransactionView() {
        cardLayout.show(mainCardPanel, TRANSACTION_VIEW);
        transactionPanel.getUpcInput().requestFocus();
    }

    /**
     * Switches to the payment view and refreshes its content.
     */
    public void showPaymentView() {
        paymentPanel.refresh();
        cardLayout.show(mainCardPanel, PAYMENT_VIEW);
    }

    /**
     * Called when payment is completed successfully.
     */
    private void onPaymentComplete() {
        showTransactionView();
    }

    // ==================== Public Methods (delegated to panels) ====================

    public void addItemToTable(String upc, String desc, double price, int qty, double total) {
        transactionPanel.addItemToTable(upc, desc, price, qty, total);
    }

    public void clearTable() {
        transactionPanel.clearTable();
    }

    public void updateTotals(double subtotal, double discount, double tax, double total) {
        transactionPanel.updateTotals(subtotal, discount, tax, total);
    }

    public void setDiscountStatus(List<String> appliedDiscounts) {
        transactionPanel.setDiscountStatus(appliedDiscounts);
    }

    public void setTransactionStatus(String status) {
        transactionPanel.setTransactionStatus(status);
    }

    public void showError(String message) {
        // Show on the currently visible panel
        if (isPaymentViewVisible()) {
            JOptionPane.showMessageDialog(paymentPanel, message, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            transactionPanel.showError(message);
        }
    }

    public void showMessage(String message) {
        // Show on the currently visible panel
        if (isPaymentViewVisible()) {
            JOptionPane.showMessageDialog(paymentPanel, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            transactionPanel.showMessage(message);
        }
    }

    /**
     * Shows payment complete dialog.
     * Now handled by PaymentPanel, but kept for backward compatibility.
     */
    public void showPaymentComplete(double subtotal, double discount, double tax,
                                    double total, double tendered, double change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Payment Complete\n\n");
        sb.append(String.format("Subtotal: $%s\n", df.format(subtotal)));
        if (discount > 0) {
            sb.append(String.format("Discount: -$%s\n", df.format(discount)));
        }
        sb.append(String.format("Tax: $%s\n", df.format(tax)));
        sb.append(String.format("Total: $%s\n\n", df.format(total)));
        sb.append(String.format("Tendered: $%s\n", df.format(tendered)));
        sb.append(String.format("Change: $%s", df.format(change)));

        JOptionPane.showMessageDialog(this, sb.toString(), "Payment Complete",
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

            String status;
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

    // ==================== Helper Methods ====================

    /**
     * Checks if the payment view is currently visible.
     */
    private boolean isPaymentViewVisible() {
        return paymentPanel.isVisible() && !transactionPanel.isVisible();
    }

    // ==================== Scanner Key Event Dispatcher ====================

    private class ScannerKeyEventDispatcher implements KeyEventDispatcher {
        private static final long SCAN_TIMEOUT = 100;
        private final StringBuilder scanBuffer = new StringBuilder();
        private long lastKeyTime = 0;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            // Only process scans on transaction view
            if (isPaymentViewVisible()) {
                return false;
            }

            if (e.getID() != KeyEvent.KEY_PRESSED) return false;

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeyTime > SCAN_TIMEOUT && !scanBuffer.isEmpty()) {
                scanBuffer.setLength(0);
            }
            lastKeyTime = currentTime;

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (!scanBuffer.isEmpty()) {
                    String scannedUPC = scanBuffer.toString().trim();
                    scanBuffer.setLength(0);
                    SwingUtilities.invokeLater(() -> {
                        controller.addItem(scannedUPC, 1);
                        transactionPanel.flashUpcField(scannedUPC);
                        transactionPanel.getQtyInput().setText("1");
                    });
                    return true;
                }
                return false;
            }

            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '-' || c == '_') {
                scanBuffer.append(c);
                return true;
            }

            return false;
        }
    }
}