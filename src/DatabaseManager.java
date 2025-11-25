import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:h2:./registerdb";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();

        // Create products table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS products (
                upc VARCHAR(50) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                price DECIMAL(10,2) NOT NULL,
                is_quick_key BOOLEAN DEFAULT FALSE,
                quick_key_position INT DEFAULT NULL
            )
        """);

        // Create transactions table with additional fields for tracking status
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transactions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                subtotal DECIMAL(10,2),
                tax DECIMAL(10,2),
                total DECIMAL(10,2),
                payment_type VARCHAR(20),
                amount_tendered DECIMAL(10,2),
                change_amount DECIMAL(10,2),
                is_voided BOOLEAN DEFAULT FALSE,
                void_date TIMESTAMP DEFAULT NULL,
                void_reason VARCHAR(255),
                is_suspended BOOLEAN DEFAULT FALSE,
                suspend_date TIMESTAMP DEFAULT NULL,
                is_resumed BOOLEAN DEFAULT FALSE,
                resume_date TIMESTAMP DEFAULT NULL,
                is_completed BOOLEAN DEFAULT FALSE,
                completion_date TIMESTAMP DEFAULT NULL
            )
        """);

        // Create transaction_items table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transaction_items (
                id INT AUTO_INCREMENT PRIMARY KEY,
                transaction_id INT,
                upc VARCHAR(50),
                product_name VARCHAR(255),
                price DECIMAL(10,2),
                quantity INT,
                total DECIMAL(10,2),
                is_voided BOOLEAN DEFAULT FALSE,
                FOREIGN KEY (transaction_id) REFERENCES transactions(id)
            )
        """);

        stmt.close();
    }

    public void loadPriceBook(Map<String, Product> products) throws SQLException {
        // Clear existing products
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM products");
        stmt.close();

        // Insert all products
        String sql = "INSERT INTO products (upc, name, price) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);

        for (Product product : products.values()) {
            pstmt.setString(1, product.getUpc());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPrice());
            pstmt.addBatch();
        }

        pstmt.executeBatch();
        pstmt.close();
    }

    public Product getProductByUPC(String upc) throws SQLException {
        String sql = "SELECT * FROM products WHERE upc = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, upc);

        ResultSet rs = pstmt.executeQuery();
        Product product = null;

        if (rs.next()) {
            product = new Product(
                    rs.getString("upc"),
                    rs.getString("name"),
                    rs.getDouble("price")
            );
        }

        rs.close();
        pstmt.close();
        return product;
    }

    public void setQuickKey(String upc, int position) throws SQLException {
        String sql = "UPDATE products SET is_quick_key = TRUE, quick_key_position = ? WHERE upc = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, position);
        pstmt.setString(2, upc);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public List<Product> getQuickKeyProducts() throws SQLException {
        String sql = "SELECT * FROM products WHERE is_quick_key = TRUE ORDER BY quick_key_position";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        List<Product> quickKeys = new ArrayList<>();
        while (rs.next()) {
            quickKeys.add(new Product(
                    rs.getString("upc"),
                    rs.getString("name"),
                    rs.getDouble("price")
            ));
        }

        rs.close();
        stmt.close();
        return quickKeys;
    }

    public int saveTransaction(double subtotal, double tax, double total) throws SQLException {
        String sql = "INSERT INTO transactions (subtotal, tax, total) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setDouble(1, subtotal);
        pstmt.setDouble(2, tax);
        pstmt.setDouble(3, total);
        pstmt.executeUpdate();

        ResultSet rs = pstmt.getGeneratedKeys();
        int transactionId = -1;
        if (rs.next()) {
            transactionId = rs.getInt(1);
        }

        rs.close();
        pstmt.close();
        return transactionId;
    }

    // Clear all non-voided items for a transaction before re-saving
    public void clearTransactionItems(int transactionId) throws SQLException {
        String sql = "DELETE FROM transaction_items WHERE transaction_id = ? AND is_voided = FALSE";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void saveTransactionItem(int transactionId, TransactionItem item) throws SQLException {
        String sql = "INSERT INTO transaction_items (transaction_id, upc, product_name, price, quantity, total) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, transactionId);
        pstmt.setString(2, item.getProduct().getUpc());
        pstmt.setString(3, item.getProduct().getName());
        pstmt.setDouble(4, item.getProduct().getPrice());
        pstmt.setInt(5, item.getQuantity());
        pstmt.setDouble(6, item.getTotal());
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void updateTransactionPayment(int transactionId, String paymentType,
                                         double tendered, double change) throws SQLException {
        String sql = "UPDATE transactions SET payment_type = ?, amount_tendered = ?, " +
                "change_amount = ?, is_completed = TRUE, completion_date = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, paymentType);
        pstmt.setDouble(2, tendered);
        pstmt.setDouble(3, change);
        pstmt.setInt(4, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void voidTransaction(int transactionId, String reason) throws SQLException {
        String sql = "UPDATE transactions SET is_voided = TRUE, void_date = CURRENT_TIMESTAMP, void_reason = ? WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, reason);
        pstmt.setInt(2, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void updateTransactionTotals(int transactionId, double subtotal, double tax, double total) throws SQLException {
        String sql = "UPDATE transactions SET subtotal = ?, tax = ?, total = ? WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setDouble(1, subtotal);
        pstmt.setDouble(2, tax);
        pstmt.setDouble(3, total);
        pstmt.setInt(4, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public void suspendTransaction(int transactionId) throws SQLException {
        // When suspending, reset the resumed flag so it can be resumed again
        String sql = "UPDATE transactions SET is_suspended = TRUE, is_resumed = FALSE, suspend_date = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, transactionId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    // Get suspended transactions that can be resumed
    public List<Integer> getSuspendedTransactions() throws SQLException {
        // Get transactions that are currently suspended and not completed or voided
        // Don't check is_resumed since a transaction can be suspended again after being resumed
        String sql = "SELECT id FROM transactions WHERE is_suspended = TRUE AND is_completed = FALSE AND is_voided = FALSE ORDER BY id DESC";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        List<Integer> suspendedIds = new ArrayList<>();
        while (rs.next()) {
            suspendedIds.add(rs.getInt("id"));
        }

        rs.close();
        stmt.close();
        return suspendedIds;
    }

    // Resume a suspended transaction
    public Map<String, Object> resumeTransaction(int transactionId) throws SQLException {
        Map<String, Object> transactionData = new HashMap<>();

        // Mark transaction as resumed and not suspended
        String updateSql = "UPDATE transactions SET is_resumed = TRUE, is_suspended = FALSE, resume_date = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement pstmt = connection.prepareStatement(updateSql);
        pstmt.setInt(1, transactionId);
        pstmt.executeUpdate();
        pstmt.close();

        // Get transaction details
        String transSql = "SELECT * FROM transactions WHERE id = ?";
        pstmt = connection.prepareStatement(transSql);
        pstmt.setInt(1, transactionId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            transactionData.put("id", rs.getInt("id"));
            transactionData.put("subtotal", rs.getDouble("subtotal"));
            transactionData.put("tax", rs.getDouble("tax"));
            transactionData.put("total", rs.getDouble("total"));
        }
        rs.close();
        pstmt.close();

        // Get transaction items
        String itemsSql = "SELECT * FROM transaction_items WHERE transaction_id = ? AND is_voided = FALSE";
        pstmt = connection.prepareStatement(itemsSql);
        pstmt.setInt(1, transactionId);
        rs = pstmt.executeQuery();

        List<TransactionItem> items = new ArrayList<>();
        while (rs.next()) {
            Product product = new Product(
                    rs.getString("upc"),
                    rs.getString("product_name"),
                    rs.getDouble("price")
            );
            TransactionItem item = new TransactionItem(product, rs.getInt("quantity"));
            items.add(item);
        }
        transactionData.put("items", items);

        rs.close();
        pstmt.close();

        return transactionData;
    }

    // Get transaction history for reporting
    public List<Map<String, Object>> getTransactionHistory(boolean includeVoided, boolean includeSuspended) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE 1=1");

        if (!includeVoided) {
            sql.append(" AND is_voided = FALSE");
        }
        if (!includeSuspended) {
            sql.append(" AND (is_suspended = FALSE OR is_resumed = TRUE OR is_completed = TRUE)");
        }

        sql.append(" ORDER BY transaction_date DESC");

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql.toString());

        List<Map<String, Object>> transactions = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> trans = new HashMap<>();
            trans.put("id", rs.getInt("id"));
            trans.put("date", rs.getTimestamp("transaction_date"));
            trans.put("subtotal", rs.getDouble("subtotal"));
            trans.put("tax", rs.getDouble("tax"));
            trans.put("total", rs.getDouble("total"));
            trans.put("payment_type", rs.getString("payment_type"));
            trans.put("is_voided", rs.getBoolean("is_voided"));
            trans.put("is_suspended", rs.getBoolean("is_suspended"));
            trans.put("is_resumed", rs.getBoolean("is_resumed"));
            trans.put("is_completed", rs.getBoolean("is_completed"));
            transactions.add(trans);
        }

        rs.close();
        stmt.close();

        return transactions;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}