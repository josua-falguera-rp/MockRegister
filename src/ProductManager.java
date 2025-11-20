import java.util.*;

public class ProductManager {
    private Map<String, Product> products;

    public ProductManager() {
        products = new HashMap<>();
    }

    public void loadPriceBook(String filePath) {
        products = PricebookParser.parseTSV(filePath);
    }

    public Product getProductByUPC(String upc) {
        return products.get(upc);
    }
}
