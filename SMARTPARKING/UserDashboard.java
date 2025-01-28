package SMARTPARKING;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDashboard extends JPanel {
    private JPanel contentPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String username;
    private int userId = -1;

    public UserDashboard(String username) {
        this.username = username;
        setLayout(new BorderLayout());
        initializeContentPanel();
        initializeSidebar();
    }

    private void initializeSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 600));
        sidebar.setBackground(new Color(35, 53, 75)); // #23354b
        sidebar.setLayout(new BorderLayout());

        // User info panel
        JPanel userInfoPanel = createUserInfoPanel();
        sidebar.add(userInfoPanel, BorderLayout.NORTH);

        // Sidebar buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1, 5, 5));
        buttonPanel.setBackground(new Color(35, 53, 75));

        JButton homeButton = createSidebarButton("Home");
        JButton parkButton = createSidebarButton("Park");
        JButton parkHistoryButton = createSidebarButton("Park History");
        JButton profileButton = createSidebarButton("Profile");

        buttonPanel.add(homeButton);
        buttonPanel.add(parkButton);
        buttonPanel.add(parkHistoryButton);
        buttonPanel.add(profileButton);

        sidebar.add(buttonPanel, BorderLayout.CENTER);

        // Add action listeners
        homeButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Home");
            setSelectedButton(homeButton);
        });

        parkButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Park");
            setSelectedButton(parkButton);
        });

        parkHistoryButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "ParkHistory");
            setSelectedButton(parkHistoryButton);
        });

        profileButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Profile");
            setSelectedButton(profileButton);
        });

        add(sidebar, BorderLayout.WEST);
    }

    private JPanel createUserInfoPanel() {
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BorderLayout());
        userInfoPanel.setBackground(new Color(35, 53, 75));
        userInfoPanel.setPreferredSize(new Dimension(250, 100));
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        // Profile picture
        JLabel profilePicLabel = new JLabel();
        profilePicLabel.setIcon(new ImageIcon(new ImageIcon("/Users/shitalyadav/Desktop/Smart Parking /SMARTPARKING/user.png")
                .getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
        profilePicLabel.setPreferredSize(new Dimension(60, 60));
        userInfoPanel.add(profilePicLabel, BorderLayout.WEST);

        // User details
        JPanel userDetailsPanel = new JPanel();
        userDetailsPanel.setLayout(new BoxLayout(userDetailsPanel, BoxLayout.Y_AXIS));
        userDetailsPanel.setBackground(new Color(35, 53, 75));

        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setForeground(Color.WHITE);
        userDetailsPanel.add(usernameLabel);

        JLabel statusLabel = new JLabel("User");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userDetailsPanel.add(statusLabel);

        userInfoPanel.add(userDetailsPanel, BorderLayout.CENTER);

        return userInfoPanel;
    }

    private void initializeContentPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add pages with the specified background color
        cardPanel.add(createHomePage(), "Home");
        cardPanel.add(createParkPage(), "Park");
        cardPanel.add(createParkHistoryPage(), "ParkHistory");
        cardPanel.add(createProfilePage(), "Profile");

        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createHomePage() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.decode("#cee6e6"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Welcome Message
        UserProfile userProfile = new UserProfile(username);
        JLabel welcomeLabel = new JLabel("Welcome, " + userProfile.getFullname() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcomeLabel, gbc);
        
        // Stats Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);
        
        // Stats components
        ParkingManager parkingManager = new ParkingManager(getUserId());
        VehicleManager vehicleManager = new VehicleManager(getUserId());
        
        JPanel expensesCard = createStatsCard("Total Expenses", 
            String.format("Rs. %.2f", parkingManager.getTotalExpenses()));
        JPanel vehiclesCard = createStatsCard("Your Vehicles", 
            String.valueOf(vehicleManager.getUserVehicles().size()));
        JPanel parksCard = createStatsCard("Total Parks", 
            String.valueOf(parkingManager.getParkingHistory().size()));
        
        statsPanel.add(expensesCard);
        statsPanel.add(vehiclesCard);
        statsPanel.add(parksCard);
        
        gbc.gridy = 1;
        gbc.weighty = 1;
        panel.add(statsPanel, gbc);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Stats");
        refreshButton.addActionListener(e -> {
            statsPanel.removeAll();
            statsPanel.add(createStatsCard("Total Expenses", 
                String.format("Rs. %.2f", parkingManager.getTotalExpenses())));
            statsPanel.add(createStatsCard("Your Vehicles", 
                String.valueOf(vehicleManager.getUserVehicles().size())));
            statsPanel.add(createStatsCard("Total Parks", 
                String.valueOf(parkingManager.getParkingHistory().size())));
            statsPanel.revalidate();
            statsPanel.repaint();
        });
        
        gbc.gridy = 2;
        gbc.weighty = 0;
        panel.add(refreshButton, gbc);
        
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

    
    private JPanel createParkHistoryPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.decode("#cde6e6"));
        
        String[] columns = {"Vehicle Number", "Date & Time", "Total Bill"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        
        // Load parking history
        ParkingManager parkingManager = new ParkingManager(getUserId());
        List<ParkingRecord> history = parkingManager.getParkingHistory();
        
        for (ParkingRecord record : history) {
            model.addRow(new Object[]{
                record.getVehicleNumber(),
                record.getParkedDateTime(),
                String.format("Rs. %.2f", record.getTotalBill())
            });
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            parkingManager.getParkingHistory().forEach(record -> 
                model.addRow(new Object[]{
                    record.getVehicleNumber(),
                    record.getParkedDateTime(),
                    String.format("Rs. %.2f", record.getTotalBill())
                })
            );
        });
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
    }

private JPanel createProfilePage() {
    JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));
    mainPanel.setBackground(Color.decode("#cee6e6"));
    
    // Left panel - User Profile
    mainPanel.add(createUserProfilePanel());
    
    // Right panel - Vehicle Management
    mainPanel.add(createVehicleManagementPanel());
    
    return mainPanel;
}

private JPanel createUserProfilePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.decode("#cee6e6"));
    panel.setBorder(BorderFactory.createTitledBorder("User Profile"));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    UserProfile userProfile = new UserProfile(username);
    
    JTextField fullnameField = new JTextField(userProfile.getFullname(), 20);
    JTextField emailField = new JTextField(userProfile.getEmail(), 20);
    JPasswordField passwordField = new JPasswordField(20);
    
    addFormField(panel, "Full Name:", fullnameField, gbc, 0);
    addFormField(panel, "Email:", emailField, gbc, 1);
    addFormField(panel, "New Password:", passwordField, gbc, 2);
    
    JButton saveButton = new JButton("Save Changes");
    gbc.gridy = 3;
    gbc.gridx = 1;
    panel.add(saveButton, gbc);
    
    saveButton.addActionListener(e -> {
        String newPassword = new String(passwordField.getPassword());
        if (userProfile.updateProfile(fullnameField.getText(), emailField.getText(), newPassword)) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile");
        }
    });
    
    return panel;
}

private JPanel createVehicleManagementPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setBackground(Color.decode("#cee6e6"));
    panel.setBorder(BorderFactory.createTitledBorder("Vehicle Management"));
    
    // Create table
    String[] columns = {"Vehicle Number", "Type", "Actions"};
    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2; // Only allow editing of action column
        }
    };
    
    JTable table = new JTable(model);
    table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
    table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox()));
    
    // Load vehicles
    VehicleManager vehicleManager = new VehicleManager(getUserId());
    updateVehicleTable(model, vehicleManager);
    
    JButton addVehicleBtn = new JButton("Add New Vehicle");
    addVehicleBtn.addActionListener(e -> {
        showAddVehicleDialog(vehicleManager, model);  // Pass model as parameter
        updateVehicleTable(model, vehicleManager);
    });
    
    // Add components
    panel.add(new JScrollPane(table), BorderLayout.CENTER);
    panel.add(addVehicleBtn, BorderLayout.SOUTH);
    
    return panel;
}

private void updateVehicleTable(DefaultTableModel model, VehicleManager vehicleManager) {
    model.setRowCount(0);
    for (Vehicle vehicle : vehicleManager.getUserVehicles()) {
        model.addRow(new Object[]{
            vehicle.getVehicleNumber(),
            vehicle.getVehicleType(),
            "Delete"
        });
    }
}

// Custom button renderer for table
class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value == null) ? "" : value.toString());
        return this;
    }
}

// Custom button editor for table
class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private JTable table;  // Add table field

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table;  // Store table reference
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }
    
    @Override
    public Object getCellEditorValue() {
        if (isPushed && table != null) {
            // Handle delete action
            int row = table.getSelectedRow();
            String vehicleNumber = (String) table.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(button, "Delete vehicle " + vehicleNumber + "?",
                    "Delete Vehicle", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                VehicleManager vehicleManager = new VehicleManager(getUserId());
                vehicleManager.deleteVehicle(vehicleNumber);
                updateVehicleTable((DefaultTableModel) table.getModel(), vehicleManager);
            }
        }
        isPushed = false;
        return label;
    }
}
private JPanel createParkPage() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.decode("#cee6e6"));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    VehicleManager vehicleManager = new VehicleManager(getUserId());
    JComboBox<Vehicle> vehicleList = new JComboBox<>();
    vehicleManager.getUserVehicles().forEach(vehicleList::addItem);
    
    // Add vehicle selector label
    JLabel vehicleLabel = new JLabel("Select Vehicle:");
    JLabel durationLabel = new JLabel("Hours to Park:");
    JLabel totalLabel = new JLabel("Total Amount: Rs. 0.00");
    
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 24, 1);
    JSpinner durationSpinner = new JSpinner(spinnerModel);
    
    // Add components using GridBagLayout
    gbc.gridx = 0; gbc.gridy = 0;
    panel.add(vehicleLabel, gbc);
    gbc.gridx = 1;
    panel.add(vehicleList, gbc);
    
    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(durationLabel, gbc);
    gbc.gridx = 1;
    panel.add(durationSpinner, gbc);
    
    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setBackground(Color.decode("#cee6e6"));
    
    JButton parkButton = new JButton("Park Vehicle");
    parkButton.addActionListener(e -> {
        Vehicle selectedVehicle = (Vehicle) vehicleList.getSelectedItem();
        int hours = (Integer) durationSpinner.getValue();
        
        if (selectedVehicle != null) {
            ParkingManager parkingManager = new ParkingManager(getUserId());
            if (parkingManager.parkVehicle(selectedVehicle.getVehicleId(), hours)) {
                JOptionPane.showMessageDialog(this, String.format(
                    "Vehicle parked successfully!\nTotal Bill: Rs. %.2f",
                    parkingManager.calculatePrice(selectedVehicle.getVehicleId(), hours)
                ));
                durationSpinner.setValue(1);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to park vehicle");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a vehicle");
        }
    });
    JButton refreshButton = new JButton("Refresh");
    
    // Add refresh functionality
    refreshButton.addActionListener(e -> {
        vehicleList.removeAllItems();
        vehicleManager.getUserVehicles().forEach(vehicleList::addItem);
    });
    
    buttonPanel.add(parkButton);
    buttonPanel.add(refreshButton);
    
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    panel.add(buttonPanel, gbc);
    
    return panel;
}


private void showAddVehicleDialog(VehicleManager vehicleManager, DefaultTableModel model) {
    JDialog dialog = new JDialog();
    dialog.setTitle("Add New Vehicle");
    dialog.setModal(true);
    dialog.setLayout(new GridBagLayout());
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    JTextField vehicleNumberField = new JTextField(15);
    JComboBox<String> vehicleTypeField = new JComboBox<>(new String[]{"car", "bike"});
    
    // Add vehicle number field
    JLabel vehicleNumberLabel = new JLabel("Vehicle Number:");
    vehicleNumberLabel.setForeground(new Color(35, 53, 75));
    gbc.gridx = 0; gbc.gridy = 0;
    dialog.add(vehicleNumberLabel, gbc);
    gbc.gridx = 1;
    dialog.add(vehicleNumberField, gbc);
    
    // Add vehicle type field
    JLabel vehicleTypeLabel = new JLabel("Vehicle Type:");
    vehicleTypeLabel.setForeground(new Color(35, 53, 75));
    gbc.gridx = 0; gbc.gridy = 1;
    dialog.add(vehicleTypeLabel, gbc);
    gbc.gridx = 1;
    dialog.add(vehicleTypeField, gbc);
    
    JButton saveButton = new JButton("Add Vehicle");
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    dialog.add(saveButton, gbc);
    
    saveButton.addActionListener(e -> {
        String vehicleNumber = vehicleNumberField.getText().trim();
        String vehicleType = (String) vehicleTypeField.getSelectedItem();
        
        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter vehicle number");
            return;
        }
        
        if (vehicleManager.addVehicle(vehicleNumber, vehicleType)) {
            JOptionPane.showMessageDialog(dialog, "Vehicle added successfully!");
            updateVehicleTable(model, vehicleManager);
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "Failed to add vehicle");
        }
    });
    
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
}




    private void addFormField(JPanel panel, String labelText, JTextField field, 
                            GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(35, 53, 75));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 40));
        button.setBackground(new Color(35, 53, 75)); // Match sidebar color #23354b
        button.setForeground(new Color(255, 255, 255)); // Keep existing text color
        button.setBorderPainted(false); // Remove border
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); // Make button transparent
        button.setOpaque(true); // Needed for background color to show
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(true);
                button.setBackground(new Color(45, 63, 85)); // Slightly lighter on hover
            }
    
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(false);
                button.setBackground(new Color(35, 53, 75));
            }
        });
        
        return button;
    }

    private void setSelectedButton(JButton selectedButton) {
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                for (Component btn : panel.getComponents()) {
                    if (btn instanceof JButton) {
                        btn.setBackground(new Color(100, 100, 100));
                    }
                }
            }
        }
        selectedButton.setBackground(new Color(150, 150, 150));
    }

    private int getUserId() {
        if (userId != -1) {
            return userId;
        }

        String query = "SELECT user_id FROM users WHERE username = ?";
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                userId = rs.getInt("user_id");
                return userId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}