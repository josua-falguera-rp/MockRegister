/**
 * DTO representing a product for discount calculation.
 * Maps to the Spring Boot API's DiscountProductDTO.
 */
public class DiscountProductDTO {

    private String upc;
    private String name;
    private double price;

    public DiscountProductDTO(String upc, String name, double price) {
        this.upc = upc;
        this.name = name;
        this.price = price;
    }

    /**
     * Factory method to convert from existing Product entity.
     */
    public static DiscountProductDTO fromProduct(Product product) {
        return new DiscountProductDTO(
                product.getUpc(),
                product.getName(),
                product.getPrice()
        );
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
        return "DiscountProductDTO{" +
                "upc='" + upc + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}