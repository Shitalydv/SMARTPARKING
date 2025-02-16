package SMARTPARKING;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class Slots extends JPanel {
    private static final int TOTAL_BIKE_SLOTS = 100;
    private static final int TOTAL_CAR_SLOTS = 50;
    private JPanel bikePanel;
    private JPanel carPanel;
    private boolean isAdmin;

    public Slots(boolean isAdmin) {
        this.isAdmin = isAdmin;
        setLayout(new BorderLayout());
        initializeSlots();
    }

    private void initializeSlots() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.decode("#cee6e6"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusPanel.setBackground(Color.decode("#cee6e6"));
        JLabel bikeLabel = new JLabel("Bike Slots Available: 0/100");
        JLabel carLabel = new JLabel("Car Slots Available: 0/50");
        statusPanel.add(bikeLabel);
        statusPanel.add(carLabel);

        // Slots Panel
        JPanel slotsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        slotsPanel.setBackground(Color.decode("#cee6e6"));

        bikePanel = createParkingGrid("Bike", 10, 10);
        carPanel = createParkingGrid("Car", 5, 10);

        slotsPanel.add(bikePanel);
        slotsPanel.add(carPanel);

        // Refresh Button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setForeground(Color.BLUE);
        refreshButton.addActionListener(e -> {
            refreshSlots();  // Refresh slot colors
            updateStatusLabels(bikeLabel, carLabel);  // Update status labels
        });

        mainPanel.add(statusPanel, BorderLayout.NORTH);
        mainPanel.add(slotsPanel, BorderLayout.CENTER);
        mainPanel.add(refreshButton, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createParkingGrid(String vehicleType, int rows, int cols) {
        JPanel panel = new JPanel(new GridLayout(rows, cols, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(vehicleType + " Parking"));
        panel.setBackground(Color.decode("#cee6e6"));

        int totalSlots = rows * cols;
        for (int i = 1; i <= totalSlots; i++) {
            JButton slot = new JButton(vehicleType.charAt(0) + String.format("%03d", i));
            slot.setPreferredSize(new Dimension(80, 60));
            slot.setFont(new Font("Arial", Font.BOLD, 12));
            slot.setOpaque(true);
            slot.setBorderPainted(true);

            boolean isOccupied = checkSlotOccupancy(vehicleType, i);
            updateSlotAppearance(slot, isOccupied);

            if (isAdmin) {
                final int slotNumber = i; // Ensure correct slot number is used in the action listener
                slot.addActionListener(e -> toggleSlotStatus(slot, vehicleType, slotNumber));
            } else {
                slot.setEnabled(false); // Disable interaction for users
            }

            panel.add(slot);
        }
        return panel;
    }

    private boolean checkSlotOccupancy(String type, int number) {
        String query = "SELECT is_occupied FROM parking_slots " +
                      "WHERE slot_number = ?";

        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, type.charAt(0) + String.format("%03d", number));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_occupied");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateSlotAppearance(JButton slot, boolean isOccupied) {
        if (isOccupied) {
            slot.setBackground(new Color(220, 53, 69));
            slot.setForeground(Color.WHITE);
            slot.setBorder(BorderFactory.createLineBorder(new Color(204, 0, 0), 2));
            
            if (isAdmin) {
                String slotDetails = getSlotDetails(slot.getText());
                ToolTipManager.sharedInstance().setDismissDelay(5000); // Set to 5 seconds
                slot.setToolTipText(slotDetails);
            } else {
                slot.setToolTipText("Occupied");
            }
        } else {
            slot.setBackground(new Color(40, 167, 69));
            slot.setForeground(Color.WHITE);
            slot.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 0), 2));
            slot.setToolTipText("Available");
        }
    }

    private void toggleSlotStatus(JButton slot, String vehicleType, int number) {
        boolean isOccupied = checkSlotOccupancy(vehicleType, number);
        String updateQuery = "UPDATE parking_slots SET is_occupied = ? WHERE slot_number = ?";

        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setBoolean(1, !isOccupied);
            pstmt.setString(2, vehicleType.charAt(0) + String.format("%03d", number));
            pstmt.executeUpdate();
            updateSlotAppearance(slot, !isOccupied);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshSlots() {
        for (Component comp : bikePanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton slot = (JButton) comp;
                String slotNumber = slot.getText();
                boolean isOccupied = checkSlotOccupancy("Bike", Integer.parseInt(slotNumber.substring(1)));
                updateSlotAppearance(slot, isOccupied);
            }
        }

        for (Component comp : carPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton slot = (JButton) comp;
                String slotNumber = slot.getText();
                boolean isOccupied = checkSlotOccupancy("Car", Integer.parseInt(slotNumber.substring(1)));
                updateSlotAppearance(slot, isOccupied);
            }
        }

        revalidate();
        repaint();
    }
    private void refreshSlots(JLabel bikeLabel, JLabel carLabel) {
        refreshSlots();  // Refresh slot colors
        updateStatusLabels(bikeLabel, carLabel);  // Update status labels
    }


    private void updateStatusLabels(JLabel bikeLabel, JLabel carLabel) {
        String countQuery = "SELECT vehicle_type, COUNT(*) as count FROM parking_slots " +
                           "WHERE is_occupied = TRUE " +
                           "GROUP BY vehicle_type";

        try (Connection conn = new DatabaseHandler().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {

            int bikeCount = 0, carCount = 0;
            while (rs.next()) {
                String vehicleType = rs.getString("vehicle_type");
                int count = rs.getInt("count");

                if ("bike".equals(vehicleType)) {
                    bikeCount = count;
                } else if ("car".equals(vehicleType)) {
                    carCount = count;
                }
            }

            bikeLabel.setText(String.format("Bike Slots Available: %3d/%d", 
                TOTAL_BIKE_SLOTS - bikeCount, TOTAL_BIKE_SLOTS));
            carLabel.setText(String.format("Car Slots Available:  %2d/%d",
                TOTAL_CAR_SLOTS - carCount, TOTAL_CAR_SLOTS));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSlotDetails(String slotNumber) {
    String query = """
        SELECT 
            u.fullname,
            u.email,
            v.vehicle_number,
            p.parked_datetime,
            TIMESTAMPDIFF(HOUR, p.parked_datetime, NOW()) as duration
        FROM parks p
        JOIN vehicles v ON p.vehicle_id = v.vehicle_id
        JOIN users u ON v.user_id = u.user_id
        WHERE p.parking_spot = ? AND p.exit_time IS NULL
    """;
    
    try (Connection conn = new DatabaseHandler().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setString(1, slotNumber);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return String.format("<html>" +
                "<div style='background-color: #f8f8f8; padding: 5px; border: 1px solid #ddd;'>" +
                "<b>User Details:</b><br>" +
                "Name: %s<br>" +
                "Email: %s<br><br>" +
                "<b>Vehicle Details:</b><br>" +
                "Number: %s<br>" +
                "Parked Time: %s<br>" +
                "Duration: %d hours" +
                "</div></html>",
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("vehicle_number"),
                sdf.format(rs.getTimestamp("parked_datetime")),
                rs.getInt("duration")
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "No details available";
}
}


// final done dashbaord and linked with userDashboard but not allowed to edit , only show 
// allowed to change by admin ( occupied to not occupied - Red to Green  &  not occupied  to occupied  - Green to Red )
// Manisha Sha
