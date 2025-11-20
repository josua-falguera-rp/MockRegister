public class TransactionItem {
    private Product product;
    private int quantity;

    public TransactionItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public void addQuantity(int qty) {
        this.quantity += qty;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return product.getPrice() * quantity;
    }
}