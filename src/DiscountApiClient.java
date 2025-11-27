import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP Client for communicating with the Discount API.
 * Uses Java 11+ HttpClient for REST calls.
 */
public class DiscountApiClient {

    private final ApiConfig config;
    private final HttpClient httpClient;

    public DiscountApiClient(ApiConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
                .build();
    }

    /**
     * Calls the discount API to calculate discounts for the given request.
     *
     * @param request The discount request containing items
     * @return DiscountResponse with calculated discounts
     * @throws DiscountApiException if the API call fails
     */
    public DiscountResponse calculateDiscount(DiscountRequest request) throws DiscountApiException {
        if (config.isEnabled()) {
            throw new DiscountApiException("Discount API is disabled");
        }

        try {
            String jsonBody = toJson(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(config.getDiscountUrl()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMillis(config.getReadTimeout()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                throw new DiscountApiException(
                        "API returned error status: " + response.statusCode() +
                                " - " + response.body()
                );
            }

        } catch (DiscountApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DiscountApiException("Failed to call discount API: " + e.getMessage(), e);
        }
    }

    /**
     * Tests if the API is reachable.
     */
    public boolean isApiAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl()))
                    .timeout(Duration.ofMillis(2000))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts DiscountRequest to JSON string.
     */
    private String toJson(DiscountRequest request) {
        StringBuilder json = new StringBuilder();
        json.append("{\"items\":[");

        List<DiscountItemDTO> items = request.getItems();
        for (int i = 0; i < items.size(); i++) {
            DiscountItemDTO item = items.get(i);
            DiscountProductDTO product = item.getProduct();

            json.append("{");
            json.append("\"product\":{");
            json.append("\"upc\":\"").append(escapeJson(product.getUpc())).append("\",");
            json.append("\"name\":\"").append(escapeJson(product.getName())).append("\",");
            json.append("\"price\":").append(product.getPrice());
            json.append("},");
            json.append("\"quantity\":").append(item.getQuantity());
            json.append("}");

            if (i < items.size() - 1) {
                json.append(",");
            }
        }

        json.append("]}");
        return json.toString();
    }

    /**
     * Parses JSON response into DiscountResponse.
     */
    private DiscountResponse parseResponse(String json) throws DiscountApiException {
        try {
            DiscountResponse response = new DiscountResponse();

            response.setOriginalTotal(extractDouble(json, "originalTotal"));
            response.setDiscountAmount(extractDouble(json, "discountAmount"));
            response.setFinalTotal(extractDouble(json, "finalTotal"));
            response.setAppliedDiscounts(extractStringList(json));

            return response;
        } catch (Exception e) {
            throw new DiscountApiException("Failed to parse API response: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a double value from JSON.
     */
    private double extractDouble(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return 0.0;

        start += pattern.length();
        int end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == ',' || c == '}') break;
            end++;
        }

        String value = json.substring(start, end).trim();
        return Double.parseDouble(value);
    }

    /**
     * Extracts a string list from JSON array.
     */
    private List<String> extractStringList(String json) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + "appliedDiscounts" + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return result;

        start = json.indexOf("[", start);
        if (start == -1) return result;

        int end = json.indexOf("]", start);
        if (end == -1) return result;

        String arrayContent = json.substring(start + 1, end).trim();
        if (arrayContent.isEmpty()) return result;

        // Parse string elements
        int pos = 0;
        while (pos < arrayContent.length()) {
            int quoteStart = arrayContent.indexOf("\"", pos);
            if (quoteStart == -1) break;

            int quoteEnd = arrayContent.indexOf("\"", quoteStart + 1);
            if (quoteEnd == -1) break;

            result.add(arrayContent.substring(quoteStart + 1, quoteEnd));
            pos = quoteEnd + 1;
        }

        return result;
    }

    /**
     * Escapes special characters for JSON.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Custom exception for API errors.
     */
    public static class DiscountApiException extends Exception {
        public DiscountApiException(String message) {
            super(message);
        }

        public DiscountApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}