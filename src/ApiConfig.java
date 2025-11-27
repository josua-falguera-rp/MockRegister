/**
 * Configuration class for the Discount API connection.
 * Centralizes all API-related settings for easy modification.
 */
public class ApiConfig {

    // API Endpoint Configuration
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DISCOUNT_ENDPOINT = "/api/v1/discount";

    // Timeout Configuration (in milliseconds)
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    // Instance fields (allow runtime configuration)
    private String baseUrl;
    private int connectTimeout;
    private int readTimeout;
    private boolean enabled;

    /**
     * Creates configuration with default settings.
     */
    public ApiConfig() {
        this.baseUrl = DEFAULT_BASE_URL;
        this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.enabled = true;
    }

    /**
     * Returns the full URL for the discount calculation endpoint.
     */
    public String getDiscountUrl() {
        return baseUrl + DISCOUNT_ENDPOINT;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public boolean isEnabled() {
        return !enabled;
    }


    @Override
    public String toString() {
        return "ApiConfig{" +
                "baseUrl='" + baseUrl + '\'' +
                ", discountUrl='" + getDiscountUrl() + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", enabled=" + enabled +
                '}';
    }
}