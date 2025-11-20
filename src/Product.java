public class Product {
    private final String upc;
    private final String name;
    private final double price;

    public Product(String upc, String name, double price) {
        this.upc = upc;
        this.name = name;
        this.price = price;
    }

    public String getUpc() {
        return upc;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    @Override
    public String toString() {
        return "Product{" + "upc=" + upc + ", name=" + name + ", price=" + price + '}';
    }
}
