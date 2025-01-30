package SMARTPARKING;

import java.sql.*;
import javax.swing.*;
import java.awt.*;

public class SmartParkingTest {
    public static void main(String[] args) {
        System.out.println("Starting Smart Parking System Tests...\n");
        
        // Test database connection
        testDatabaseConnection();
        
        // Test user registration
        testUserRegistration();
        
        // Test login
        testLogin();
        
        // Test vehicle management
        testVehicleManagement();
        
        // Test parking operations
        testParkingOperations();

        // Test UI
        testUI();
    }
    
    private static void testDatabaseConnection() {
        System.out.println("Testing Database Connection:");
        try {
            DatabaseHandler dbHandler = new DatabaseHandler();
            Connection conn = dbHandler.getConnection();
            System.out.println("✓ Database connection successful");
            conn.close();
        } catch (SQLException e) {
            System.out.println("✗ Database connection failed: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void testUserRegistration() {
        System.out.println("Testing User Registration:");
        RegistrationHandler registrationHandler = new RegistrationHandler();
        
        // Test user registration
        String testUsername = "testuser_" + System.currentTimeMillis();
        boolean regResult = registrationHandler.registerUser(
            "Test User",
            testUsername,
            "test@example.com",
            "Test@123",
            false
        );
        
        System.out.println(regResult ? 
            "✓ User registration successful" : 
            "✗ User registration failed");
        System.out.println();
    }
    
    private static void testLogin() {
        System.out.println("Testing Login:");
        LoginHandler loginHandler = new LoginHandler();
        
        // Test valid login
        boolean loginResult = loginHandler.validateLogin("testuser", "Test@123");
        System.out.println(loginResult ? 
            "✓ Login successful" : 
            "✗ Login failed");
        
        // Test invalid login
        boolean invalidLogin = loginHandler.validateLogin("invalid", "wrong");
        System.out.println(!invalidLogin ? 
            "✓ Invalid login rejected" : 
            "✗ Invalid login accepted");
        System.out.println();
    }
    
    private static void testVehicleManagement() {
        System.out.println("Testing Vehicle Management:");
        
        // Get a test user ID
        int userId = getUserIdForTest();
        if (userId == -1) {
            System.out.println("✗ Could not get test user ID");
            return;
        }
        
        VehicleManager vehicleManager = new VehicleManager(userId);
        
        // Test adding vehicle
        String testVehicleNumber = "TEST" + System.currentTimeMillis();
        boolean addResult = vehicleManager.addVehicle(testVehicleNumber, "car");
        System.out.println(addResult ? 
            "✓ Vehicle added successfully" : 
            "✗ Failed to add vehicle");
        
        // Test getting vehicles
        int vehicleCount = vehicleManager.getUserVehicles().size();
        System.out.println(vehicleCount > 0 ? 
            "✓ Retrieved user vehicles: " + vehicleCount : 
            "✗ Failed to retrieve vehicles");
        
        // Test deleting vehicle
        boolean deleteResult = vehicleManager.deleteVehicle(testVehicleNumber);
        System.out.println(deleteResult ? 
            "✓ Vehicle deleted successfully" : 
            "✗ Failed to delete vehicle");
        System.out.println();
    }
    
    private static void testParkingOperations() {
        System.out.println("Testing Parking Operations:");
        
        int userId = getUserIdForTest();
        if (userId == -1) {
            System.out.println("✗ Could not get test user ID");
            return;
        }
        
        // Add test vehicle first
        VehicleManager vehicleManager = new VehicleManager(userId);
        String testVehicleNumber = "PARK" + System.currentTimeMillis();
        vehicleManager.addVehicle(testVehicleNumber, "car");
        
        // Get vehicle ID
        int vehicleId = getVehicleId(testVehicleNumber);
        if (vehicleId == -1) {
            System.out.println("✗ Could not get test vehicle ID");
            return;
        }
        
        ParkingManager parkingManager = new ParkingManager(userId);
        
        // Test parking vehicle
        boolean parkResult = parkingManager.parkVehicle(vehicleId, 2);
        System.out.println(parkResult ? 
            "✓ Vehicle parked successfully" : 
            "✗ Failed to park vehicle");
        
        // Test getting parking history
        int historyCount = parkingManager.getParkingHistory().size();
        System.out.println(historyCount > 0 ? 
            "✓ Retrieved parking history: " + historyCount : 
            "✗ Failed to retrieve parking history");
        
        // Test calculating price
        double price = parkingManager.calculatePrice(vehicleId, 2);
        System.out.println(price > 0 ? 
            "✓ Price calculation successful: Rs. " + price : 
            "✗ Failed to calculate price");
        
        // Cleanup
        vehicleManager.deleteVehicle(testVehicleNumber);
        System.out.println();
    }

    private static void testUI() {
        System.out.println("Testing UI:");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("✗ Failed to set look and feel: " + e.getMessage());
            }
            JFrame frame = new JFrame("Smart Parking Test UI");
            frame.setSize(400, 300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());

            JLabel label = new JLabel("Smart Parking System UI Test");
            JButton button = new JButton("Click Me");

            button.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Button clicked!"));

            frame.add(label);
            frame.add(button);
            frame.setVisible(true);
        });
    }
    
    private static int getUserIdForTest() {
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM users WHERE username = ?")) {
            
            pstmt.setString(1, "testuser");
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private static int getVehicleId(String vehicleNumber) {
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT vehicle_id FROM vehicles WHERE vehicle_number = ?")) {
            
            pstmt.setString(1, vehicleNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("vehicle_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
