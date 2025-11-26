/**
 * DTO representing a transaction line item for discount calculation.
 * Maps to the Spring Boot API's DiscountItemDTO.
 */
public class DiscountItemDTO {

    private DiscountProductDTO product;
    private int quantity;

    public DiscountItemDTO() {
    }

    public DiscountItemDTO(DiscountProductDTO product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Factory method to convert from existing TransactionItem entity.
     */
    public static DiscountItemDTO fromTransactionItem(TransactionItem item) {
        return new DiscountItemDTO(
                DiscountProductDTO.fromProduct(item.getProduct()),
                item.getQuantity()
        );
    }

    public DiscountProductDTO getProduct() {
        return product;
    }

    public void setProduct(DiscountProductDTO product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public String toString() {
        return "DiscountItemDTO{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", lineTotal=" + getLineTotal() +
                '}';
    }
}