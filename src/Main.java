import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize product manager and load pricebook
        ProductManager productManager = new ProductManager();
        productManager.loadPriceBook("src/pricebook__1_.tsv");

        // Create controller
        RegisterController controller = new RegisterController(productManager);

        // Create and show UI
        SwingUtilities.invokeLater(() -> {
            RegisterUI ui = new RegisterUI(controller);
            controller.setUI(ui);
            ui.setVisible(true);
        });
    }
}