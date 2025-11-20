import java.util.*;

public class ProductManager {
    private Map<String, Product> products;

    public ProductManager() {
        products = new HashMap<>();
    }

    public void loadPriceBook(String filePath) {
        products = PricebookParser.parseTSV(filePath);
        System.out.println("Loaded " + products.size() + " products from pricebook");
    }

    public Product getProductByUPC(String upc) {
        return products.get(upc);
    }

    public Collection<Product> getAllProducts() {
        return products.values();
    }

    public boolean hasProduct(String upc) {
        return products.containsKey(upc);
    }

    public int getProductCount() {
        return products.size();
    }
}
