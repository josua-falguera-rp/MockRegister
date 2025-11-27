import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
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

    public void showTransactionHistory(List<Map<String, Object>> history) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            parentFrame = JOptionPane.getFrameForComponent(this);
        }

        TransactionHistoryDialog dialog = new TransactionHistoryDialog(parentFrame, history);
        dialog.setVisible(true);
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