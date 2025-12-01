// src/VirtualJournal.java
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Virtual Journal that logs transactions both locally and to a remote server.
 * Sends formatted strings directly to the server - no parsing required.
 */
public class VirtualJournal {
    private static final String JOURNAL_FILE = "register_journal.txt";
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BufferedWriter writer;
    private VirtualJournalSocketClient socketClient;
    private SocketClientConfig socketConfig;

    public VirtualJournal() {
        try {
            writer = new BufferedWriter(new FileWriter(JOURNAL_FILE, true));
        } catch (IOException e) {
            System.err.println("Error opening journal file: " + e.getMessage());
        }

        // Initialize socket client
        socketConfig = new SocketClientConfig();
        socketClient = new VirtualJournalSocketClient(socketConfig);

        // Attempt to connect to remote server
        if (socketConfig.isEnabled()) {
            boolean connected = socketClient.connect();
            if (!connected) {
                System.err.println("Failed to connect to Virtual Journal server, will log locally only");
            }
        }
    }

    /**
     * Sends a line to the remote server if connected.
     */
    private void sendToRemoteServer(String line) {
        if (socketClient.isConnected()) {
            socketClient.sendJournalLine(line);
        }
    }

    public void logTransactionStart(int transactionId) {
        String line1 = "=".repeat(60);
        String line2 = "TRANSACTION #" + transactionId + " - " + dateFormat.format(new Date());
        String line3 = "=".repeat(60);

        writeLine(line1);
        writeLine(line2);
        writeLine(line3);

        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
        sendToRemoteServer(line3);
    }

    public void logItem(String upc, String name, double price) {
        String line = String.format("%-20s %-30s $%-8s",
                upc, truncate(name), df.format(price));
        writeLine(line);
        sendToRemoteServer(line);
    }

    public void logVoidItem(String upc, String name, int qty) {
        String line = "*** VOID ITEM: " + upc + " " + name + " QTY: " + qty + " ***";
        writeLine(line);
        sendToRemoteServer(line);
    }

    public void logQuantityChange(String upc, String name, int oldQty, int newQty) {
        String line = "*** QTY CHANGE: " + upc + " " + name + " FROM " + oldQty + " TO " + newQty + " ***";
        writeLine(line);
        sendToRemoteServer(line);
    }

    public void logSubtotal(double subtotal) {
        writeLine("");
        String line = String.format("%50s $%s", "SUBTOTAL:", df.format(subtotal));
        writeLine(line);

        sendToRemoteServer("");
        sendToRemoteServer(line);
    }

    public void logDiscount(double discountAmount, List<String> appliedDiscounts) {
        if (discountAmount > 0) {
            String line1 = String.format("%50s -$%s", "DISCOUNT:", df.format(discountAmount));
            writeLine(line1);
            sendToRemoteServer(line1);

            for (String discount : appliedDiscounts) {
                String line = String.format("%50s   %s", "", discount);
                writeLine(line);
                sendToRemoteServer(line);
            }
        }
    }

    public void logTax(double tax) {
        String line = String.format("%50s $%s", "TAX (7%):", df.format(tax));
        writeLine(line);
        sendToRemoteServer(line);
    }

    public void logTotal(double total) {
        String line1 = String.format("%50s $%s", "TOTAL:", df.format(total));
        String line2 = "-".repeat(60);

        writeLine(line1);
        writeLine(line2);

        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
    }

    public void logPayment(String paymentType, double tendered, double change) {
        writeLine("");
        String line1 = "PAYMENT TYPE: " + paymentType;
        String line2 = String.format("%50s $%s", "AMOUNT TENDERED:", df.format(tendered));

        writeLine(line1);
        writeLine(line2);

        sendToRemoteServer("");
        sendToRemoteServer(line1);
        sendToRemoteServer(line2);

        if (change > 0) {
            String line3 = String.format("%50s $%s", "CHANGE:", df.format(change));
            writeLine(line3);
            sendToRemoteServer(line3);
        }
    }

    public void logVoidTransaction(int transactionId) {
        writeLine("");
        String line1 = "*** TRANSACTION #" + transactionId + " VOIDED ***";
        String line2 = "*** VOIDED AT: " + dateFormat.format(new Date()) + " ***";
        String line3 = "=".repeat(60);
        writeLine(line1);
        writeLine(line2);
        writeLine(line3);
        writeLine("");

        sendToRemoteServer("");
        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
        sendToRemoteServer(line3);
        sendToRemoteServer("");
    }

    public void logSuspendTransaction(int transactionId) {
        writeLine("");
        String line1 = "*** TRANSACTION #" + transactionId + " SUSPENDED ***";
        String line2 = "*** SUSPENDED AT: " + dateFormat.format(new Date()) + " ***";
        String line3 = "=".repeat(60);
        writeLine(line1);
        writeLine(line2);
        writeLine(line3);
        writeLine("");

        sendToRemoteServer("");
        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
        sendToRemoteServer(line3);
        sendToRemoteServer("");
    }

    public void logResumeTransaction(int transactionId) {
        writeLine("");
        String line1 = "*** TRANSACTION #" + transactionId + " RESUMED ***";
        String line2 = "*** RESUMED AT: " + dateFormat.format(new Date()) + " ***";
        String line3 = "=".repeat(60);
        writeLine(line1);
        writeLine(line2);
        writeLine(line3);

        sendToRemoteServer("");
        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
        sendToRemoteServer(line3);
    }

    public void logTransactionComplete(int transactionId) {
        writeLine("");
        String line1 = "TRANSACTION #" + transactionId + " COMPLETED";
        String line2 = "COMPLETED AT: " + dateFormat.format(new Date());
        String line3 = "=".repeat(60);
        writeLine(line1);
        writeLine(line2);
        writeLine(line3);
        writeLine("");
        flush();

        sendToRemoteServer("");
        sendToRemoteServer(line1);
        sendToRemoteServer(line2);
        sendToRemoteServer(line3);
        sendToRemoteServer("");
    }

    private void writeLine(String text) {
        try {
            writer.write(text);
            writer.newLine();
            System.out.println(text); // Also print to console
        } catch (IOException e) {
            System.err.println("Error writing to journal: " + e.getMessage());
        }
    }

    private String truncate(String text) {
        if (text.length() <= 30) {
            return text;
        }
        return text.substring(0, 30 - 3) + "...";
    }

    public void flush() {
        try {
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error flushing journal: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing journal: " + e.getMessage());
        }

        // Disconnect from remote server
        if (socketClient != null) {
            socketClient.disconnect();
        }
    }
}