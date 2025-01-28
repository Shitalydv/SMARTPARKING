package SMARTPARKING;

import java.sql.*;
import java.util.*;

public class VehicleManager {
    private int userId;
    
    public VehicleManager(int userId) {
        this.userId = userId;
    }
    
    public boolean addVehicle(String vehicleNumber, String vehicleType) {
        String query = "INSERT INTO vehicles (vehicle_number, vehicle_type, user_id) VALUES (?, ?, ?)";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, vehicleNumber);
            pstmt.setString(2, vehicleType);
            pstmt.setInt(3, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Vehicle> getUserVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String query = "SELECT * FROM vehicles WHERE user_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                vehicles.add(new Vehicle(
                    rs.getInt("vehicle_id"),
                    rs.getString("vehicle_number"),
                    rs.getString("vehicle_type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }
    public boolean deleteVehicle(String vehicleNumber) {
        String query = "DELETE FROM vehicles WHERE vehicle_number = ? AND user_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, vehicleNumber);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

class Vehicle {
    private int vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    
    public Vehicle(int vehicleId, String vehicleNumber, String vehicleType) {
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
    }
    
    public int getVehicleId() { return vehicleId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    
    @Override
    public String toString() {
        return vehicleNumber + " (" + vehicleType + ")";
    }
}