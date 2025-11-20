import java.util.*;

public class RegisterController {
    private ProductManager productManager;
    private List<TransactionItem> currentTransaction;
    private RegisterUI ui;

    public RegisterController(ProductManager productManager) {
        this.productManager = productManager;
        this.currentTransaction = new ArrayList<>();
    }

    public void setUI(RegisterUI ui) {
        this.ui = ui;
    }

    public void addItem(String upc, int qty) {
        Product product = productManager.getProductByUPC(upc);

        if (product == null) {
            ui.showError("Product not found with UPC: " + upc);
            return;
        }

        // Check if item already exists in transaction
        TransactionItem existingItem = findItemByUPC(upc);

        if (existingItem != null) {
            existingItem.addQuantity(qty);
        } else {
            // Add new item
            TransactionItem newItem = new TransactionItem(product, qty);
            currentTransaction.add(newItem);
        }

        // Update UI
        ui.clearTable();
        for (TransactionItem item : currentTransaction) {
            ui.addItemToTable(
                    item.getProduct().getUpc(),
                    item.getProduct().getName(),
                    item.getProduct().getPrice(),
                    item.getQuantity(),
                    item.getTotal()
            );
        }
    }

    private TransactionItem findItemByUPC(String upc) {
        for (TransactionItem item : currentTransaction) {
            if (item.getProduct().getUpc().equals(upc)) {
                return item;
            }
        }
        return null;
    }

    public double getTotal() {
        double total = 0;
        for (TransactionItem item : currentTransaction) {
            total += item.getTotal();
        }
        return total;
    }
}