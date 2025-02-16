package SMARTPARKING;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;



public class UserDashboard extends JPanel {
    private JPanel contentPanel;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String username;
    private int userId = -1;
    private Slots slotsPanel;

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
        JButton slotsButton = createSidebarButton("Slots");

        buttonPanel.add(homeButton);
        buttonPanel.add(parkButton);
        buttonPanel.add(parkHistoryButton);
        buttonPanel.add(profileButton);
        buttonPanel.add(slotsButton);

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

        slotsButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Slots");
            setSelectedButton(slotsButton);
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
        profilePicLabel.setIcon(new ImageIcon(new ImageIcon("/Users/shitalyadav/Desktop/Smart Parking /SMARTPARKING/user1.png")
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
        slotsPanel = new Slots(false);

        // Add pages with the specified background color
        cardPanel.add(createHomePage(), "Home");
        cardPanel.add(createParkPage(), "Park");
        cardPanel.add(createParkHistoryPage(), "ParkHistory");
        cardPanel.add(createProfilePage(), "Profile");
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
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font style
        refreshButton.setPreferredSize(new Dimension(150, 30)); // Set size
        refreshButton.setForeground(Color.BLUE);
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
        
        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setPreferredSize(new Dimension(150, 30));
        logoutButton.setForeground(Color.RED);
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
                );
    
            if (choice == JOptionPane.YES_OPTION) {
             SmartParkingApp.showLoginPage();
            }
    });
    gbc.gridy = 3;
    gbc.gridwidth = 0;
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
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.decode("#cde6e6"));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {
            "Vehicle Number", 
            "Entry Time", 
            "Exit Time", 
            "Parking Spot", 
            "Total Bill"
        };
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setGridColor(Color.LIGHT_GRAY);
        
        // Create custom header renderer
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setFont(new Font("Arial", Font.BOLD, 12));
    
        // Apply renderer to each column header
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setDefaultRenderer(headerRenderer);
    
        updateParkingHistoryTable(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh History");
        refreshBtn.setForeground(Color.BLUE);
        refreshBtn.addActionListener(e -> updateParkingHistoryTable(model));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.decode("#cde6e6"));
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void updateParkingHistoryTable(DefaultTableModel model) {
        model.setRowCount(0);
        String query = """
            SELECT 
                v.vehicle_number, 
                p.parked_datetime, 
                p.exit_time, 
                p.parking_spot, 
                p.total_bill 
            FROM parks p 
            JOIN vehicles v ON p.vehicle_id = v.vehicle_id 
            WHERE v.user_id = ? 
            ORDER BY 
                CASE WHEN p.exit_time IS NULL THEN 1 ELSE 0 END,
                p.parked_datetime DESC
        """;
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, getUserId());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("vehicle_number"),
                    formatDateTime(rs.getTimestamp("parked_datetime")),
                    formatDateTime(rs.getTimestamp("exit_time")),
                    rs.getString("parking_spot"),
                    String.format("Rs. %.2f", rs.getDouble("total_bill"))
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading parking history", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "Active";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(timestamp);
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
    
    JLabel vehicleLabel = new JLabel("Select Vehicle:");
    JLabel durationLabel = new JLabel("Hours to Park:");
    JLabel totalLabel = new JLabel("Total Amount: Rs. 0.00");
    
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 24, 1);
    JSpinner durationSpinner = new JSpinner(spinnerModel);
    
    vehicleList.addActionListener(e -> {
        Vehicle selectedVehicle = (Vehicle) vehicleList.getSelectedItem();
        if (selectedVehicle != null) {
            ParkingManager parkingManager = new ParkingManager(getUserId());
            double amount = parkingManager.calculatePrice(selectedVehicle.getVehicleId(), 
                (Integer) durationSpinner.getValue());
            totalLabel.setText(String.format("Total Amount: Rs. %.2f", amount));
        }
    });
    
    durationSpinner.addChangeListener(e -> {
        Vehicle selectedVehicle = (Vehicle) vehicleList.getSelectedItem();
        if (selectedVehicle != null) {
            ParkingManager parkingManager = new ParkingManager(getUserId());
            double amount = parkingManager.calculatePrice(selectedVehicle.getVehicleId(), 
                (Integer) durationSpinner.getValue());
            totalLabel.setText(String.format("Total Amount: Rs. %.2f", amount));
        }
    });
    
    gbc.gridx = 0; gbc.gridy = 0;
    panel.add(vehicleLabel, gbc);
    gbc.gridx = 1;
    panel.add(vehicleList, gbc);
    
    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(durationLabel, gbc);
    gbc.gridx = 1;
    panel.add(durationSpinner, gbc);
    
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.gridwidth = 2;
    panel.add(totalLabel, gbc);
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.setBackground(Color.decode("#cee6e6"));
    
    JButton parkButton = new JButton("Park Vehicle");
    JButton refreshButton = new JButton("Refresh");
    refreshButton.setForeground(Color.BLUE);
    
    // Create Active Parkings Panel
    JPanel activeParkingsPanel = new JPanel();
    activeParkingsPanel.setLayout(new BoxLayout(activeParkingsPanel, BoxLayout.Y_AXIS));
    activeParkingsPanel.setBorder(BorderFactory.createTitledBorder("Active Parkings"));
    activeParkingsPanel.setBackground(Color.decode("#cee6e6"));
    
    loadActiveParkings(activeParkingsPanel);

    parkButton.addActionListener(e -> {
        Vehicle selectedVehicle = (Vehicle) vehicleList.getSelectedItem();
        int hours = (Integer) durationSpinner.getValue();
        
        if (selectedVehicle != null) {
            ParkingManager parkingManager = new ParkingManager(getUserId());
            
            if (parkingManager.isVehicleParked(selectedVehicle.getVehicleId())) {
                JOptionPane.showMessageDialog(this, 
                    "This vehicle is already parked. Please exit first.", 
                    "Vehicle Already Parked", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String allocatedSlot = parkingManager.parkVehicle(selectedVehicle.getVehicleId(), hours);
            
            if (allocatedSlot != null) {
                double bill = parkingManager.calculatePrice(selectedVehicle.getVehicleId(), hours);
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Vehicle parked successfully!\nAllocated Slot: %s\nTotal Bill: Rs. %.2f", 
                    allocatedSlot, bill));
                
                // Create exit button with panel
                JPanel parkingEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
                parkingEntry.setBackground(Color.WHITE);
                parkingEntry.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                
                JLabel parkingInfo = new JLabel(String.format("%s - Slot: %s", 
                    selectedVehicle.getVehicleNumber(), allocatedSlot));
                JButton exitButton = new JButton("Exit");
                exitButton.setForeground(Color.RED);
                
                exitButton.addActionListener(exitEvent -> {
                    if (parkingManager.exitVehicle(selectedVehicle.getVehicleId())) {
                        JOptionPane.showMessageDialog(this, "Vehicle exited successfully!");
                        activeParkingsPanel.remove(parkingEntry);
                        activeParkingsPanel.revalidate();
                        activeParkingsPanel.repaint();
                        slotsPanel.refreshSlots();
                    }
                });
                
                parkingEntry.add(parkingInfo);
                parkingEntry.add(exitButton);
                activeParkingsPanel.add(parkingEntry);
                activeParkingsPanel.revalidate();
                activeParkingsPanel.repaint();
                
                // Reset and refresh
                slotsPanel.refreshSlots();
                durationSpinner.setValue(1);
            } else {
                JOptionPane.showMessageDialog(this, "No available slots!");
            }
        }
    });
    
    refreshButton.addActionListener(e -> {
        vehicleList.removeAllItems();
        vehicleManager.getUserVehicles().forEach(vehicleList::addItem);
        buttonPanel.removeAll();
        buttonPanel.add(parkButton);
        buttonPanel.add(refreshButton);
        buttonPanel.revalidate();
        buttonPanel.repaint();
    });
    
    buttonPanel.add(parkButton);
    buttonPanel.add(refreshButton);
    
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    panel.add(buttonPanel, gbc);
    
    gbc.gridy = 4;
    panel.add(activeParkingsPanel, gbc);
    
    return panel;
}

private void loadActiveParkings(JPanel activeParkingsPanel) {
    ParkingManager parkingManager = new ParkingManager(getUserId());
    String query = "SELECT p.vehicle_id, v.vehicle_number, p.parking_spot FROM parks p " +
                  "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                  "WHERE v.user_id = ? AND p.exit_time IS NULL";
                  
    try (Connection conn = new DatabaseHandler().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setInt(1, getUserId());
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            String vehicleNumber = rs.getString("vehicle_number");
            String parkingSpot = rs.getString("parking_spot");
            int vehicleId = rs.getInt("vehicle_id");
            
            JPanel parkingEntry = new JPanel(new FlowLayout(FlowLayout.LEFT));
            parkingEntry.setBackground(Color.WHITE);
            parkingEntry.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            
            JLabel parkingInfo = new JLabel(String.format("%s - Slot: %s", 
                vehicleNumber, parkingSpot));
            JButton exitButton = new JButton("Exit");
            exitButton.setForeground(Color.RED);
            
            exitButton.addActionListener(e -> {
                if (parkingManager.exitVehicle(vehicleId)) {
                    JOptionPane.showMessageDialog(this, "Vehicle exited successfully!");
                    activeParkingsPanel.remove(parkingEntry);
                    activeParkingsPanel.revalidate();
                    activeParkingsPanel.repaint();
                    slotsPanel.refreshSlots();
                }
            });
            
            parkingEntry.add(parkingInfo);
            parkingEntry.add(exitButton);
            activeParkingsPanel.add(parkingEntry);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Error loading active parkings", 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
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

    private JPanel createSlotsPage() {
        return new Slots(false); // User can only view slots
    }
}


// done userDashboard including slots view  
// Shital Yadav 