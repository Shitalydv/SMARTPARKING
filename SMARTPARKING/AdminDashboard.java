package SMARTPARKING;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

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
        buttonPanel.setLayout(new GridLayout(4, 1, 5, 5));
        buttonPanel.setBackground(new Color(35, 53, 75));
        
        addNavigationButton(buttonPanel, "Home");
        addNavigationButton(buttonPanel, "Users");
        addNavigationButton(buttonPanel, "Rates");
        addNavigationButton(buttonPanel, "History");
        
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
        
        add(cardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHomePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        
        // Total Users
        int userCount = getTotalUsers();
        statsPanel.add(createStatsCard("Total Users", String.valueOf(userCount)));
        
        // Total Revenue
        double revenue = getTotalRevenue();
        statsPanel.add(createStatsCard("Total Revenue", String.format("Rs. %.2f", revenue)));
        
        // Total Parks
        int parkCount = getTotalParks();
        statsPanel.add(createStatsCard("Total Parks", String.valueOf(parkCount)));
        
        panel.add(statsPanel);
        return panel;
    }
    
    private JPanel createUsersPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        String[] columns = {"User ID", "Name", "Email", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        loadUsers(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadUsers(model));
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
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
}