import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VirtualJournal {
    private static final String JOURNAL_FILE = "register_journal.txt";
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BufferedWriter writer;

    public VirtualJournal() {
        try {
            writer = new BufferedWriter(new FileWriter(JOURNAL_FILE, true));
        } catch (IOException e) {
            System.err.println("Error opening journal file: " + e.getMessage());
        }
    }

    public void logTransactionStart(int transactionId) {
        writeLine("=".repeat(60));
        writeLine("TRANSACTION #" + transactionId + " - " + dateFormat.format(new Date()));
        writeLine("=".repeat(60));
    }

    public void logItem(String upc, String name, double price, int qty, double total) {
        String line = String.format("%-20s %-30s $%-8s x%-3d $%s",
                upc, truncate(name), df.format(price), qty, df.format(total));
        writeLine(line);
    }

    public void logVoidItem(String upc, String name, int qty) {
        writeLine("*** VOID ITEM: " + upc + " " + name + " QTY: " + qty + " ***");
    }

    public void logQuantityChange(String upc, String name, int oldQty, int newQty) {
        writeLine("*** QTY CHANGE: " + upc + " " + name + " FROM " + oldQty + " TO " + newQty + " ***");
    }

    public void logSubtotal(double subtotal) {
        writeLine("");
        writeLine(String.format("%50s $%s", "SUBTOTAL:", df.format(subtotal)));
    }

    public void logTax(double tax) {
        writeLine(String.format("%50s $%s", "TAX (7%):", df.format(tax)));
    }

    public void logTotal(double total) {
        writeLine(String.format("%50s $%s", "TOTAL:", df.format(total)));
        writeLine("-".repeat(60));
    }

    public void logPayment(String paymentType, double tendered, double change) {
        writeLine("");
        writeLine("PAYMENT TYPE: " + paymentType);
        writeLine(String.format("%50s $%s", "AMOUNT TENDERED:", df.format(tendered)));
        if (change > 0) {
            writeLine(String.format("%50s $%s", "CHANGE:", df.format(change)));
        }
    }

    public void logVoidTransaction(int transactionId) {
        writeLine("");
        writeLine("*** TRANSACTION #" + transactionId + " VOIDED ***");
        writeLine("=".repeat(60));
        writeLine("");
    }

    public void logSuspendTransaction(int transactionId) {
        writeLine("");
        writeLine("*** TRANSACTION #" + transactionId + " SUSPENDED ***");
        writeLine("=".repeat(60));
        writeLine("");
    }

    public void logTransactionComplete(int transactionId) {
        writeLine("");
        writeLine("TRANSACTION #" + transactionId + " COMPLETED");
        writeLine("=".repeat(60));
        writeLine("");
        flush();
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
    }
}