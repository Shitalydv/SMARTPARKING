package SMARTPARKING;

import java.sql.*;
import java.util.*;

public class ParkingManager {
    private int userId;

    public ParkingManager(int userId) {
        this.userId = userId;
    }

    public boolean parkVehicle(int vehicleId, int hours) {
        String query = "INSERT INTO parks (vehicle_id, hourly_rate, total_bill) " +
                      "SELECT ?, hr.hourly_rate, (hr.hourly_rate * ?) " +
                      "FROM vehicles v " +
                      "JOIN hourly_rates hr ON v.vehicle_type = hr.vehicle_type " +
                      "WHERE v.vehicle_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, vehicleId);
            pstmt.setInt(2, hours);
            pstmt.setInt(3, vehicleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ParkingRecord> getParkingHistory() {
        List<ParkingRecord> history = new ArrayList<>();
        String query = "SELECT v.vehicle_number, p.parked_datetime, p.total_bill " +
                      "FROM parks p " +
                      "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                      "WHERE v.user_id = ? " +
                      "ORDER BY p.parked_datetime DESC";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                history.add(new ParkingRecord(
                    rs.getString("vehicle_number"),
                    rs.getTimestamp("parked_datetime"),
                    rs.getDouble("total_bill")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public double calculatePrice(int vehicleId, int hours) {
        String query = "SELECT hr.hourly_rate * ? as total " +
                      "FROM vehicles v " +
                      "JOIN hourly_rates hr ON v.vehicle_type = hr.vehicle_type " +
                      "WHERE v.vehicle_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, hours);
            pstmt.setInt(2, vehicleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalExpenses() {
        String query = "SELECT SUM(total_bill) as total FROM parks p " +
                      "JOIN vehicles v ON p.vehicle_id = v.vehicle_id " +
                      "WHERE v.user_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
}

class ParkingRecord {
    private String vehicleNumber;
    private Timestamp entryTime;
    private double totalBill;

    public ParkingRecord(String vehicleNumber, Timestamp entryTime, double totalBill) {
        this.vehicleNumber = vehicleNumber;
        this.entryTime = entryTime;
        this.totalBill = totalBill;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public Timestamp getParkedDateTime() { return entryTime; }
    public double getTotalBill() { return totalBill; }
}
