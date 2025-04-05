import java.sql.*;

public class UserAccount {
    private final String accountNumber;

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/atm_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "lohit123"; // Update this if needed

    // Load JDBC driver when class is loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Please add it to your classpath.");
            e.printStackTrace();
        }
    }

    public UserAccount(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    // Static method to authenticate user
    public static boolean authenticate(String acc, String pin) throws SQLException {
        String query = "SELECT 1 FROM users WHERE account_number = ? AND pin = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, acc);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    // Fetch current balance
    public double getBalanceFromDB() throws SQLException {
        String query = "SELECT balance FROM users WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                throw new SQLException("Account not found.");
            }
        }
    }

    // Deposit money to user's account
    public void depositToDB(double amount) throws SQLException {
        String query = "UPDATE users SET balance = balance + ? WHERE account_number = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Deposit failed. Account not found.");
            }
        }
    }

    // Withdraw money from user's account
    public boolean withdrawFromDB(double amount) throws SQLException {
        String selectQuery = "SELECT balance FROM users WHERE account_number = ?";
        String updateQuery = "UPDATE users SET balance = balance - ? WHERE account_number = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false); // Begin transaction

            // Check balance
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setString(1, accountNumber);
                ResultSet rs = selectStmt.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    throw new SQLException("Account not found.");
                }

                double balance = rs.getDouble("balance");

                if (balance < amount) {
                    conn.rollback();
                    return false; // Insufficient balance
                }
            }

            // Proceed with withdrawal
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, amount);
                updateStmt.setString(2, accountNumber);
                updateStmt.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            throw e; // Let the caller handle it
        }
    }
}
