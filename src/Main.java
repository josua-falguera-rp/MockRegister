import javax.swing.*;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Initialize database and journal
        DatabaseManager dbManager = new DatabaseManager();
        VirtualJournal journal = new VirtualJournal();

        // Load pricebook from TSV file
        Map<String, Product> products = PricebookParser.parseTSV("src/pricebook__1_.tsv");

        try {
            // Store products in database
            dbManager.loadPriceBook(products);
            System.out.println("Loaded " + products.size() + " products into database");
        } catch (Exception e) {
            System.err.println("Error loading pricebook: " + e.getMessage());
            e.printStackTrace();
        }

        // Create controller
        RegisterController controller = new RegisterController(dbManager, journal);

        // Create and show UI
        SwingUtilities.invokeLater(() -> {
            RegisterUI ui = new RegisterUI(controller);
            controller.setUI(ui);
            ui.setVisible(true);
        });

        // Add shutdown hook to close database and journal
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            journal.close();
            dbManager.close();
            System.out.println("Database and journal closed");
        }));
    }
}