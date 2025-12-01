// src/SocketClientConfig.java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration for the Virtual Journal socket client.
 * Allows easy configuration of server IP and port.
 */
public class SocketClientConfig {

    private static final String CONFIG_FILE = "register-config.properties";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9090;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 300000;
    private static final int DEFAULT_RETRY_ATTEMPTS = 3;
    private static final int DEFAULT_RETRY_DELAY = 2000;

    private String serverHost;
    private int serverPort;
    private int connectTimeout;
    private int readTimeout;
    private int retryAttempts;
    private int retryDelay;
    private boolean enabled;
    private String registerId;

    public SocketClientConfig() {
        loadConfiguration();
    }

    /**
     * Loads configuration from properties file or creates default.
     */
    private void loadConfiguration() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);

            serverHost = props.getProperty("vj.server.host", DEFAULT_HOST);
            serverPort = Integer.parseInt(props.getProperty("vj.server.port", String.valueOf(DEFAULT_PORT)));
            connectTimeout = Integer.parseInt(props.getProperty("vj.connect.timeout", String.valueOf(DEFAULT_CONNECT_TIMEOUT)));
            readTimeout = Integer.parseInt(props.getProperty("vj.read.timeout", String.valueOf(DEFAULT_READ_TIMEOUT)));
            retryAttempts = Integer.parseInt(props.getProperty("vj.retry.attempts", String.valueOf(DEFAULT_RETRY_ATTEMPTS)));
            retryDelay = Integer.parseInt(props.getProperty("vj.retry.delay", String.valueOf(DEFAULT_RETRY_DELAY)));
            enabled = Boolean.parseBoolean(props.getProperty("vj.enabled", "true"));
            registerId = props.getProperty("vj.register.id", "REG-001");

            System.out.println("Loaded configuration from " + CONFIG_FILE);

        } catch (IOException e) {
            System.out.println("Configuration file not found, creating default: " + CONFIG_FILE);
            setDefaults();
            saveConfiguration();
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in configuration, using defaults");
            setDefaults();
        }
    }

    /**
     * Sets default configuration values.
     */
    private void setDefaults() {
        serverHost = DEFAULT_HOST;
        serverPort = DEFAULT_PORT;
        connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        readTimeout = DEFAULT_READ_TIMEOUT;
        retryAttempts = DEFAULT_RETRY_ATTEMPTS;
        retryDelay = DEFAULT_RETRY_DELAY;
        enabled = true;
        registerId = "REG-001";
    }

    /**
     * Saves current configuration to properties file.
     */
    public void saveConfiguration() {
        Properties props = new Properties();
        props.setProperty("vj.server.host", serverHost);
        props.setProperty("vj.server.port", String.valueOf(serverPort));
        props.setProperty("vj.connect.timeout", String.valueOf(connectTimeout));
        props.setProperty("vj.read.timeout", String.valueOf(readTimeout));
        props.setProperty("vj.retry.attempts", String.valueOf(retryAttempts));
        props.setProperty("vj.retry.delay", String.valueOf(retryDelay));
        props.setProperty("vj.enabled", String.valueOf(enabled));
        props.setProperty("vj.register.id", registerId);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Virtual Journal Client Configuration");
            System.out.println("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "SocketClientConfig{" +
                "serverHost='" + serverHost + '\'' +
                ", serverPort=" + serverPort +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", retryAttempts=" + retryAttempts +
                ", retryDelay=" + retryDelay +
                ", enabled=" + enabled +
                ", registerId='" + registerId + '\'' +
                '}';
    }
}