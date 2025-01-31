package SMARTPARKING;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

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
        refreshButton.addActionListener(e -> refreshSlots(bikeLabel, carLabel));

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
            slot.setBackground(new Color(220, 53, 69)); // Red
            slot.setForeground(Color.WHITE);
            slot.setBorder(BorderFactory.createLineBorder(new Color(204, 0, 0), 2));
            slot.setToolTipText("Occupied");
        } else {
            slot.setBackground(new Color(40, 167, 69)); // Green
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

    private void refreshSlots(JLabel bikeLabel, JLabel carLabel) {
        // Refresh the existing slots in both panels
        for (Component comp : bikePanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton slot = (JButton) comp;
                int slotNumber = Integer.parseInt(slot.getText().substring(1));
                boolean isOccupied = checkSlotOccupancy("Bike", slotNumber);
                updateSlotAppearance(slot, isOccupied);
            }
        }

        for (Component comp : carPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton slot = (JButton) comp;
                int slotNumber = Integer.parseInt(slot.getText().substring(1));
                boolean isOccupied = checkSlotOccupancy("Car", slotNumber);
                updateSlotAppearance(slot, isOccupied);
            }
        }

        // Update status labels for bike and car slots
        updateStatusLabels(bikeLabel, carLabel);

        // Revalidate and repaint panels to reflect the changes
        bikePanel.revalidate();
        bikePanel.repaint();
        carPanel.revalidate();
        carPanel.repaint();
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
}


// final done dashbaord and linked with userDashboard but not allowed to edit , only show 
// allowed to change by admin ( occupied to not occupied - Red to Green  &  not occupied  to occupied  - Green to Red )
// Manisha Sha
