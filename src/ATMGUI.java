import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class ATMGUI extends JFrame {
    private UserAccount currentUser;
    private JTextField accountField;
    private JPasswordField pinField;
    private JLabel messageLabel;
    private JPanel loginPanel, mainPanel;
    private JLabel balanceLabel;

    public ATMGUI() {
        setTitle("ATM Simulator");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createLoginPanel();
        createMainPanel();

        add(loginPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loginPanel.add(new JLabel("Account Number:"));
        accountField = new JTextField();
        loginPanel.add(accountField);

        loginPanel.add(new JLabel("PIN:"));
        pinField = new JPasswordField();
        loginPanel.add(pinField);

        JButton loginButton = new JButton("Login");
        messageLabel = new JLabel("", SwingConstants.CENTER);
        loginButton.addActionListener(e -> authenticateUser());

        loginPanel.add(loginButton);
        loginPanel.add(messageLabel);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        balanceLabel = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(balanceLabel);

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton logoutButton = new JButton("Logout");

        depositButton.addActionListener(e -> deposit());
        withdrawButton.addActionListener(e -> withdraw());
        logoutButton.addActionListener(e -> logout());

        mainPanel.add(depositButton);
        mainPanel.add(withdrawButton);
        mainPanel.add(logoutButton);
    }

    private void authenticateUser() {
        String accountNumber = accountField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        try {
            if (UserAccount.authenticate(accountNumber, pin)) {
                currentUser = new UserAccount(accountNumber);
                switchToMainPanel();
            } else {
                messageLabel.setText("Invalid account number or PIN.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    private void switchToMainPanel() {
        remove(loginPanel);
        add(mainPanel, BorderLayout.CENTER);
        updateBalance();
        revalidate();
        repaint();
    }

    private void updateBalance() {
        try {
            double balance = currentUser.getBalanceFromDB();
            balanceLabel.setText("Balance: $" + String.format("%.2f", balance));
        } catch (SQLException e) {
            balanceLabel.setText("Error fetching balance.");
            e.printStackTrace();
        }
    }

    private void deposit() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount > 0) {
                    try {
                        currentUser.depositToDB(amount);
                        updateBalance();
                        JOptionPane.showMessageDialog(this, "Deposit successful.");
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Database error during deposit.");
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.");
            }
        }
    }

    private void withdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount > 0) {
                    try {
                        if (currentUser.withdrawFromDB(amount)) {
                            updateBalance();
                            JOptionPane.showMessageDialog(this, "Withdrawal successful.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Insufficient balance.");
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, "Database error during withdrawal.");
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount entered.");
            }
        }
    }

    private void logout() {
        currentUser = null;
        remove(mainPanel);
        add(loginPanel, BorderLayout.CENTER);
        accountField.setText("");
        pinField.setText("");
        messageLabel.setText("");
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ATMGUI::new);
    }
}
