import java.io.*;
import java.util.*;

public class PricebookParser {
    public static Map<String, Product> parseTSV(String filePath) {
        Map<String, Product> products = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\t");

                if (columns.length >= 3) {
                    String upc = columns[0].trim();
                    String name = columns[1].trim();
                    double price = Double.parseDouble(columns[2].trim());

                    Product product = new Product(upc, name, price);
                    products.put(upc, product);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing price: " + e.getMessage());
        }

        return products;
    }
}
