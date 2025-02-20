package SMARTPARKING;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class AdminDashboard extends JPanel {
    private JPanel contentPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String username;
    
    public AdminDashboard(String username) {
        this.username = username;
        setLayout(new BorderLayout());
        initializeContentPanel();
        initializeSidebar();
    }
    
    private void initializeSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setBackground(new Color(35, 53, 75));
        sidebar.setLayout(new BorderLayout());
        
        // Admin info panel
        JPanel adminInfo = new JPanel();
        adminInfo.setLayout(new BorderLayout());
        adminInfo.setBackground(new Color(35, 53, 75));
        adminInfo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel adminLabel = new JLabel("Admin Dashboard");
        adminLabel.setForeground(Color.WHITE);
        adminLabel.setFont(new Font("Arial", Font.BOLD, 20));
        adminInfo.add(adminLabel, BorderLayout.CENTER);
        
        sidebar.add(adminInfo, BorderLayout.NORTH);
        
        // Navigation buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 5, 5));
        buttonPanel.setBackground(new Color(35, 53, 75));
        
        addNavigationButton(buttonPanel, "Home");
        addNavigationButton(buttonPanel, "Users");
        addNavigationButton(buttonPanel, "Rates");
        addNavigationButton(buttonPanel, "History");
        addNavigationButton(buttonPanel, "Slots");
        
        sidebar.add(buttonPanel, BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST);
    }
    
    private void addNavigationButton(JPanel panel, String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(35, 53, 75));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(e -> cardLayout.show(cardPanel, text));
        panel.add(button);
    }
    
    private void initializeContentPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        cardPanel.add(createHomePage(), "Home");
        cardPanel.add(createUsersPage(), "Users");
        cardPanel.add(createRatesPage(), "Rates");
        cardPanel.add(createHistoryPage(), "History");
        cardPanel.add(createSlotsPage(), "Slots");
        
        add(cardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHomePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Welcome Message
        JLabel welcomeLabel = new JLabel("Welcome, Admin!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);
        
        // Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        
        // Stats components
        int userCount = getTotalUsers();
        statsPanel.add(createStatsCard("Total Users", String.valueOf(userCount)));
        
        double revenue = getTotalRevenue();
        statsPanel.add(createStatsCard("Total Revenue", String.format("Rs. %.2f", revenue)));
        
        int parkCount = getTotalParks();
        statsPanel.add(createStatsCard("Total Parks", String.valueOf(parkCount)));
        
        gbc.gridy = 1;
        gbc.weighty = 1;
        panel.add(statsPanel, gbc);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Stats");
        refreshButton.setForeground(Color.BLUE);
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font style
        refreshButton.setPreferredSize(new Dimension(150, 30)); // Set size
        refreshButton.setForeground(Color.BLUE);
        refreshButton.addActionListener(e -> {
            statsPanel.removeAll();
            statsPanel.add(createStatsCard("Total Users", String.valueOf(getTotalUsers())));
            statsPanel.add(createStatsCard("Total Revenue", String.format("Rs. %.2f", getTotalRevenue())));
            statsPanel.add(createStatsCard("Total Parks", String.valueOf(getTotalParks())));
            statsPanel.revalidate();
            statsPanel.repaint();
        });
        
        gbc.gridy = 2;
        gbc.weighty = 0;
        panel.add(refreshButton, gbc);
        
        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setForeground(Color.RED);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font style to match refresh button
        logoutButton.setPreferredSize(new Dimension(150, 30)); // Set size to match refresh button
        logoutButton.addActionListener(e -> logout());
       
        gbc.gridy = 3;
        gbc.gridwidth = 0; // Ensure grid width is set correctly
        panel.add(logoutButton, gbc);
        
        return panel;
    }
    
    private void logout() {
        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        topFrame.dispose(); // Close the current window
        
        // Create and show the login page
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(1000, 600); // Set appropriate size
        loginFrame.setLocationRelativeTo(null); // Center the window
        loginFrame.add(new LoginPage(loginFrame)); // Assuming LoginPage is a JPanel
        loginFrame.setVisible(true);
    }
    
   private JPanel createUsersPage() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.decode("#cee6e6"));
    
    String[] columns = {"User ID", "Name", "Email", "Actions"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);
    JTable table = new JTable(model);
    
    // Custom renderer for Delete button
    table.getColumnModel().getColumn(3).setCellRenderer((t, value, isSelected, hasFocus, row, col) -> {
        JButton btn = new JButton("Delete");
        btn.setForeground(Color.RED);
        return btn;
    });
    
    // Add click listener for Delete action
    table.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            
            if (row >= 0 && col == 3) {  // Delete column
                int userId = (int)table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to delete this user?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteUser(userId);
                    loadUsers(model);
                }
            }
        }
    });
    
    loadUsers(model);
    
    JScrollPane scrollPane = new JScrollPane(table);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    JButton refreshBtn = new JButton("Refresh");
    refreshBtn.setForeground(Color.BLUE);
    refreshBtn.addActionListener(e -> loadUsers(model));
    panel.add(refreshBtn, BorderLayout.SOUTH);
    
    return panel;
}

private void deleteUser(int userId) {
    String query = "DELETE FROM users WHERE user_id = ?";
    try (Connection conn = new DatabaseHandler().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, userId);
        int result = pstmt.executeUpdate();
        if (result > 0) {
            JOptionPane.showMessageDialog(this, "User deleted successfully");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error deleting user");
    }
}
    
    private JPanel createRatesPage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Rate update form
        Map<String, Double> rates = getCurrentRates();
        
        JTextField bikeRateField = new JTextField(String.valueOf(rates.get("bike")), 10);
        JTextField carRateField = new JTextField(String.valueOf(rates.get("car")), 10);
        
        panel.add(new JLabel("Bike Rate (per hour):"), gbc);
        gbc.gridx = 1;
        panel.add(bikeRateField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Car Rate (per hour):"), gbc);
        gbc.gridx = 1;
        panel.add(carRateField, gbc);
        
        JButton updateBtn = new JButton("Update Rates");
        updateBtn.setForeground(Color.BLUE);
        updateBtn.addActionListener(e -> {
            try {
                double bikeRate = Double.parseDouble(bikeRateField.getText());
                double carRate = Double.parseDouble(carRateField.getText());
                updateRates(bikeRate, carRate);
                JOptionPane.showMessageDialog(this, "Rates updated successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            }
        });
        
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(updateBtn, gbc);
        
        return panel;
    }
    
    private JPanel createHistoryPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        String[] columns = {"User", "Vehicle", "Date", "Amount"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        loadParkingHistory(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setForeground(Color.BLUE);
        refreshBtn.addActionListener(e -> loadParkingHistory(model));
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatsCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        card.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    // Database operations
    private int getTotalUsers() {
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users WHERE is_admin = FALSE")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private double getTotalRevenue() {
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total_bill) as total FROM parks")) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
    private int getTotalParks() {
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM parks")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void loadUsers(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE is_admin = FALSE")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    "Delete"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Map<String, Double> getCurrentRates() {
        Map<String, Double> rates = new HashMap<>();
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM hourly_rates")) {
            while (rs.next()) {
                rates.put(rs.getString("vehicle_type"), rs.getDouble("hourly_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }
    
    private void updateRates(double bikeRate, double carRate) {
        String query = "UPDATE hourly_rates SET hourly_rate = ? WHERE vehicle_type = ?";
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Update bike rate
            pstmt.setDouble(1, bikeRate);
            pstmt.setString(2, "bike");
            pstmt.executeUpdate();
            
            // Update car rate
            pstmt.setDouble(1, carRate);
            pstmt.setString(2, "car");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadParkingHistory(DefaultTableModel model) {
        model.setRowCount(0);
        String query = "SELECT u.fullname, v.vehicle_number, p.parked_datetime, p.total_bill " +
                      "FROM parks p " +
                      "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                      "JOIN users u ON v.user_id = u.user_id " +
                      "ORDER BY p.parked_datetime DESC";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("fullname"),
                    rs.getString("vehicle_number"),
                    rs.getTimestamp("parked_datetime"),
                    String.format("Rs. %.2f", rs.getDouble("total_bill"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private JPanel createSlotsPage() {
        return new Slots(true); // Admin can modify slots
    }
}
// Manisha Sah 
// Final done 