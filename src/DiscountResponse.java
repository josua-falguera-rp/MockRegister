import java.util.Collections;
import java.util.List;

/**
 * DTO representing the response from the discount calculation API.
 * Maps to the Spring Boot API's DiscountResponse.
 */
public class DiscountResponse {

    private double originalTotal;
    private double discountAmount;
    private double finalTotal;
    private List<String> appliedDiscounts;

    public DiscountResponse() {
        this.appliedDiscounts = Collections.emptyList();
    }

    public DiscountResponse(double originalTotal, double discountAmount,
                            double finalTotal, List<String> appliedDiscounts) {
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.appliedDiscounts = appliedDiscounts != null ? appliedDiscounts : Collections.emptyList();
    }

    /**
     * Creates a response with no discounts applied (fallback scenario).
     */
    public static DiscountResponse noDiscount(double total) {
        return new DiscountResponse(total, 0.0, total, Collections.emptyList());
    }

    public double getOriginalTotal() {
        return originalTotal;
    }

    public void setOriginalTotal(double originalTotal) {
        this.originalTotal = originalTotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public List<String> getAppliedDiscounts() {
        return appliedDiscounts;
    }

    public void setAppliedDiscounts(List<String> appliedDiscounts) {
        this.appliedDiscounts = appliedDiscounts != null ? appliedDiscounts : Collections.emptyList();
    }

    public boolean hasDiscounts() {
        return discountAmount > 0 && !appliedDiscounts.isEmpty();
    }

    @Override
    public String toString() {
        return "DiscountResponse{" +
                "originalTotal=" + originalTotal +
                ", discountAmount=" + discountAmount +
                ", finalTotal=" + finalTotal +
                ", appliedDiscounts=" + appliedDiscounts +
                '}';
    }
}