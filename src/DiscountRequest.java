import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representing the request payload for discount calculation.
 * Maps to the Spring Boot API's DiscountRequest.
 */
public class DiscountRequest {

    private List<DiscountItemDTO> items;

    public DiscountRequest() {
    }

    public DiscountRequest(List<DiscountItemDTO> items) {
        this.items = items;
    }

    /**
     * Factory method to convert from a list of TransactionItem entities.
     */
    public static DiscountRequest fromTransactionItems(List<TransactionItem> transactionItems) {
        List<DiscountItemDTO> dtoItems = transactionItems.stream()
                .map(DiscountItemDTO::fromTransactionItem)
                .collect(Collectors.toList());
        return new DiscountRequest(dtoItems);
    }

    public List<DiscountItemDTO> getItems() {
        return items;
    }

    public void setItems(List<DiscountItemDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "DiscountRequest{" +
                "items=" + items +
                '}';
    }
}