// src/VirtualJournalSocketClient.java
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Simplified socket client for connecting to the Virtual Journal server.
 * Uses fire-and-forget messaging - no ACK/PONG responses needed.
 */
public class VirtualJournalSocketClient {

    private final SocketClientConfig config;
    private Socket socket;
    private PrintWriter out;
    private volatile boolean connected = false;

    public VirtualJournalSocketClient(SocketClientConfig config) {
        this.config = config;
    }

    /**
     * Connects to the Virtual Journal server.
     */
    public boolean connect() {
        if (!config.isEnabled()) {
            System.out.println("Virtual Journal client is disabled in configuration");
            return false;
        }

        int attempts = 0;
        while (attempts < config.getRetryAttempts() && !connected) {
            attempts++;
            try {
                System.out.println("Attempting to connect to Virtual Journal server: " +
                        config.getServerHost() + ":" + config.getServerPort() +
                        " (Attempt " + attempts + "/" + config.getRetryAttempts() + ")");

                socket = new Socket();
                socket.connect(
                        new java.net.InetSocketAddress(config.getServerHost(), config.getServerPort()),
                        config.getConnectTimeout()
                );
                socket.setSoTimeout(config.getReadTimeout());

                out = new PrintWriter(socket.getOutputStream(), true);

                connected = true;
                System.out.println("Successfully connected to Virtual Journal server");
                return true;

            } catch (SocketTimeoutException e) {
                System.err.println("Connection timeout (attempt " + attempts + ")");
            } catch (IOException e) {
                System.err.println("Connection failed (attempt " + attempts + "): " + e.getMessage());
            }

            if (attempts < config.getRetryAttempts()) {
                try {
                    Thread.sleep(config.getRetryDelay());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.err.println("Failed to connect to Virtual Journal server after " + attempts + " attempts");
        return false;
    }

    /**
     * Sends a journal line to the server.
     * Format: registerId|content
     * Fire-and-forget - no response expected.
     */
    public boolean sendJournalLine(String line) {
        if (!isConnected()) {
            return false;
        }

        try {
            out.println(line);

            // Check if the output stream is still valid
            if (out.checkError()) {
                System.err.println("Error sending journal line - connection may be lost");
                handleDisconnection();
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error sending journal line: " + e.getMessage());
            handleDisconnection();
            return false;
        }
    }

    /**
     * Handles unexpected disconnection.
     */
    private void handleDisconnection() {
        connected = false;
        System.err.println("Connection to Virtual Journal server lost");
        closeResources();
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        out.flush();
        closeResources();
        System.out.println("Disconnected from Virtual Journal server");
    }

    /**
     * Closes all resources.
     */
    private void closeResources() {
        connected = false;

        try {
            if (out != null) out.close();
        } catch (Exception e) {
            System.err.println("Error closing output stream: " + e.getMessage());
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Returns whether the client is currently connected.
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}