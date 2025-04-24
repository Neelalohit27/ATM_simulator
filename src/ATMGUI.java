import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ATMGUI extends JFrame {
    private UserAccount currentUser;
    private JTextField accountField;
    private JPasswordField pinField;
    private JLabel messageLabel;
    private JPanel loginPanel, mainPanel;
    private JLabel balanceLabel;

    private final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private final Color ACCENT_COLOR = new Color(76, 175, 80);
    private final Color TEXT_COLOR = Color.WHITE;

    public ATMGUI() {
        setTitle("ATM Simulator");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 12));

        createLoginPanel();
        createMainPanel();

        add(loginPanel);
        setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        ImageIcon icon = new ImageIcon("resources/atm_logo.png");
        JLabel logoLabel = new JLabel(icon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(logoLabel);
        loginPanel.add(Box.createVerticalStrut(10));

        JLabel heading = new JLabel("Welcome to ATM");
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(heading);
        loginPanel.add(Box.createVerticalStrut(20));

        JLabel accLabel = new JLabel("Account Number:");
        accLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(accLabel);

        accountField = new JTextField();
        accountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        loginPanel.add(accountField);

        JLabel pinLabel = new JLabel("PIN:");
        pinLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        loginPanel.add(pinLabel);

        pinField = new JPasswordField();
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        loginPanel.add(pinField);

        JButton loginButton = new JButton("Login");
        styleButton(loginButton, PRIMARY_COLOR);
        loginButton.addActionListener(e -> authenticateUser());
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(loginButton);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(messageLabel);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        balanceLabel = new JLabel("Balance: $0.00", SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(balanceLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        JButton depositButton = new JButton("Deposit");
        styleButton(depositButton, ACCENT_COLOR);
        depositButton.addActionListener(e -> deposit());
        mainPanel.add(depositButton);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton withdrawButton = new JButton("Withdraw");
        styleButton(withdrawButton, PRIMARY_COLOR);
        withdrawButton.addActionListener(e -> withdraw());
        mainPanel.add(withdrawButton);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton historyButton = new JButton("Transaction History");
        styleButton(historyButton, new Color(255, 152, 0));
        historyButton.addActionListener(e -> showTransactionHistory());
        mainPanel.add(historyButton);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton exportButton = new JButton("Print History (.txt)");
        styleButton(exportButton, new Color(244, 67, 54));
        exportButton.addActionListener(e -> exportTransactionHistory());
        mainPanel.add(exportButton);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton changePinButton = new JButton("Change PIN");
        styleButton(changePinButton, new Color(156, 39, 176));
        changePinButton.addActionListener(e -> changePin());
        mainPanel.add(changePinButton);
        mainPanel.add(Box.createVerticalStrut(10));

        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, Color.DARK_GRAY);
        logoutButton.addActionListener(e -> logout());
        mainPanel.add(logoutButton);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
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
        getContentPane().removeAll();
        add(mainPanel);
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
                    currentUser.depositToDB(amount);
                    updateBalance();
                    JOptionPane.showMessageDialog(this, "Deposit successful.");
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                }
            } catch (NumberFormatException | SQLException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount or database error.");
                e.printStackTrace();
            }
        }
    }

    private void withdraw() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input.trim());
                if (amount > 0) {
                    if (currentUser.withdrawFromDB(amount)) {
                        updateBalance();
                        JOptionPane.showMessageDialog(this, "Withdrawal successful.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Insufficient balance.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a positive amount.");
                }
            } catch (NumberFormatException | SQLException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount or database error.");
                e.printStackTrace();
            }
        }
    }

    private void showTransactionHistory() {
        try {
            List<String> history = currentUser.getTransactionHistory();
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No recent transactions.");
            } else {
                JTextArea textArea = new JTextArea();
                history.forEach(line -> textArea.append(line + "\n"));
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Transaction History", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching transaction history.");
            e.printStackTrace();
        }
    }

    private void exportTransactionHistory() {
        try {
            List<String> history = currentUser.getTransactionHistory();
            if (history.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions to export.");
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("transaction_history.txt"))) {
                for (String line : history) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            JOptionPane.showMessageDialog(this, "Transaction history exported to transaction_history.txt");
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting transaction history.");
            e.printStackTrace();
        }
    }

    private void changePin() {
        JPasswordField newPinField = new JPasswordField();
        JPasswordField confirmPinField = new JPasswordField();
        Object[] fields = {
            "New PIN:", newPinField,
            "Confirm PIN:", confirmPinField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Change PIN", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newPin = new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword());

            if (!newPin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(this, "PINs do not match.");
            } else if (newPin.length() < 4 || !newPin.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "PIN must be at least 4 digits.");
            } else {
                try {
                    currentUser.changePin(newPin);
                    JOptionPane.showMessageDialog(this, "PIN changed successfully.");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error changing PIN.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void logout() {
        currentUser = null;
        getContentPane().removeAll();
        add(loginPanel);
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