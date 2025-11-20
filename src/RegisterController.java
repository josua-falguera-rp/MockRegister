import java.util.*;

public class RegisterController {
    private final ProductManager productManager;
    private final List<TransactionItem> currentTransaction;
    private RegisterUI ui;

    public RegisterController(ProductManager productManager) {
        this.productManager = productManager;
        this.currentTransaction = new ArrayList<>();
    }

    public void setUI(RegisterUI ui) {
        this.ui = ui;
    }

    public void addItem(String upc, int qty) {
        Product product = findProduct(upc);
        if (product == null) {
            return;
        }

        addOrUpdateTransactionItem(product, qty);
        refreshUI();
    }

    private Product findProduct(String upc) {
        Product product = productManager.getProductByUPC(upc);
        if (product == null) {
            ui.showError("Product not found with UPC: " + upc);
        }
        return product;
    }

    private void addOrUpdateTransactionItem(Product product, int qty) {
        TransactionItem existingItem = findItemByUPC(product.getUpc());

        if (existingItem != null) {
            existingItem.addQuantity(qty);
        } else {
            currentTransaction.add(new TransactionItem(product, qty));
        }

        // Log to console
        logProductToConsole(product, qty);
    }

    private void logProductToConsole(Product product, int qty) {
        System.out.println("UPC: " + product.getUpc() + ", Description: " + product.getName() + ", Price: " + product.getPrice() + ", QTY: " + qty);
    }

    private void refreshUI() {
        ui.clearTable();
        currentTransaction.forEach(item ->
                ui.addItemToTable(
                        item.getProduct().getUpc(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getTotal()
                )
        );
    }

    private TransactionItem findItemByUPC(String upc) {
        return currentTransaction.stream()
                .filter(item -> item.getProduct().getUpc().equals(upc))
                .findFirst()
                .orElse(null);
    }

    public double getTotal() {
        return currentTransaction.stream()
                .mapToDouble(TransactionItem::getTotal)
                .sum();
    }
}