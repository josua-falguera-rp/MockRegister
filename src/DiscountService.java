import java.util.List;

/**
 * Service layer for discount calculations.
 * Wraps the API client and provides fallback behavior when API is unavailable.
 */
public class DiscountService {

    private final DiscountApiClient apiClient;
    private final ApiConfig config;
    private boolean lastCallSuccessful = true;

    public DiscountService(ApiConfig config) {
        this.config = config;
        this.apiClient = new DiscountApiClient(config);
    }

    /**
     * Calculates discounts for the given transaction items.
     * Falls back to no discount if API is unavailable.
     *
     * @param items List of transaction items
     * @return DiscountResult containing the response and status
     */
    public DiscountResult calculateDiscount(List<TransactionItem> items) {
        if (items == null || items.isEmpty()) {
            return DiscountResult.noItems();
        }

        if (!config.isEnabled()) {
            return DiscountResult.disabled(calculateSubtotal(items));
        }

        try {
            DiscountRequest request = DiscountRequest.fromTransactionItems(items);
            DiscountResponse response = apiClient.calculateDiscount(request);
            lastCallSuccessful = true;
            return DiscountResult.success(response);

        } catch (DiscountApiClient.DiscountApiException e) {
            lastCallSuccessful = false;
            System.err.println("Discount API error: " + e.getMessage());

            // Fallback: return no discount
            double subtotal = calculateSubtotal(items);
            return DiscountResult.fallback(subtotal, e.getMessage());
        }
    }

    /**
     * Checks if the discount API is currently available.
     */
    public boolean isApiAvailable() {
        return config.isEnabled() && apiClient.isApiAvailable();
    }

    /**
     * Returns whether the last API call was successful.
     */
    public boolean isLastCallSuccessful() {
        return lastCallSuccessful;
    }

    /**
     * Enables or disables the discount service.
     */
    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
    }

    /**
     * Checks if the service is enabled.
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }

    /**
     * Calculates subtotal from transaction items (fallback calculation).
     */
    private double calculateSubtotal(List<TransactionItem> items) {
        return items.stream()
                .mapToDouble(TransactionItem::getTotal)
                .sum();
    }

    /**
     * Result wrapper that includes status information.
     */
    public static class DiscountResult {
        private final DiscountResponse response;
        private final Status status;
        private final String message;

        public enum Status {
            SUCCESS,      // API call succeeded
            FALLBACK,     // API failed, using fallback
            DISABLED,     // Service is disabled
            NO_ITEMS      // No items to calculate
        }

        private DiscountResult(DiscountResponse response, Status status, String message) {
            this.response = response;
            this.status = status;
            this.message = message;
        }

        public static DiscountResult success(DiscountResponse response) {
            return new DiscountResult(response, Status.SUCCESS, "Discount calculated successfully");
        }

        public static DiscountResult fallback(double subtotal, String errorMessage) {
            DiscountResponse response = DiscountResponse.noDiscount(subtotal);
            return new DiscountResult(response, Status.FALLBACK,
                    "API unavailable, no discount applied: " + errorMessage);
        }

        public static DiscountResult disabled(double subtotal) {
            DiscountResponse response = DiscountResponse.noDiscount(subtotal);
            return new DiscountResult(response, Status.DISABLED, "Discount service is disabled");
        }

        public static DiscountResult noItems() {
            DiscountResponse response = new DiscountResponse(0, 0, 0, null);
            return new DiscountResult(response, Status.NO_ITEMS, "No items in transaction");
        }

        public DiscountResponse getResponse() {
            return response;
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccessful() {
            return status == Status.SUCCESS;
        }

        public boolean hasDiscount() {
            return response != null && response.hasDiscounts();
        }

        public double getOriginalTotal() {
            return response != null ? response.getOriginalTotal() : 0;
        }

        public double getDiscountAmount() {
            return response != null ? response.getDiscountAmount() : 0;
        }

        public double getFinalTotal() {
            return response != null ? response.getFinalTotal() : 0;
        }

        public List<String> getAppliedDiscounts() {
            return response != null ? response.getAppliedDiscounts() : List.of();
        }

        @Override
        public String toString() {
            return "DiscountResult{" +
                    "status=" + status +
                    ", message='" + message + '\'' +
                    ", hasDiscount=" + hasDiscount() +
                    ", discountAmount=" + getDiscountAmount() +
                    '}';
        }
    }
}