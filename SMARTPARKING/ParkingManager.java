package SMARTPARKING;

import java.sql.*;
import java.util.*;

public class ParkingManager {
    private int userId;

    public ParkingManager(int userId) {
        this.userId = userId;
    }

    private String findAvailableSlot(String vehicleType) {
        String query = "SELECT slot_number FROM parking_slots " +
                      "WHERE vehicle_type = ? AND is_occupied = FALSE " +
                      "ORDER BY slot_number LIMIT 1";
                  
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, vehicleType.toLowerCase());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("slot_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean parkVehicle(int vehicleId, int hours) {
        String getTypeQuery = "SELECT vehicle_type FROM vehicles WHERE vehicle_id = ?";
        String parkQuery = "INSERT INTO parks (vehicle_id, parking_spot, hourly_rate, total_bill) " +
                          "VALUES (?, ?, ?, ?)";
        String findSlotQuery = "{CALL assign_nearest_slot(?, ?)}";
    
        try (Connection conn = new DatabaseHandler().getConnection()) {
            conn.setAutoCommit(false);
    
            try {
                // Get vehicle type
                String vehicleType;
                try (PreparedStatement pstmt = conn.prepareStatement(getTypeQuery)) {
                    pstmt.setInt(1, vehicleId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) return false;
                    vehicleType = rs.getString("vehicle_type");
                }
    
                // Find available slot
                String slotNumber;
                try (CallableStatement cstmt = conn.prepareCall(findSlotQuery)) {
                    cstmt.setString(1, vehicleType);
                    cstmt.registerOutParameter(2, Types.VARCHAR);
                    cstmt.execute();
                    slotNumber = cstmt.getString(2);
                    if (slotNumber == null) {
                        throw new SQLException("No available parking slots");
                    }
                }
    
                // Calculate rate and total
                double rate = vehicleType.equals("bike") ? 30.0 : 50.0;
                double total = rate * hours;
    
                // Park vehicle
                try (PreparedStatement pstmt = conn.prepareStatement(parkQuery)) {
                    pstmt.setInt(1, vehicleId);
                    pstmt.setString(2, slotNumber);
                    pstmt.setDouble(3, rate);
                    pstmt.setDouble(4, total);
                    pstmt.executeUpdate();
                }
    
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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

// Final done 
// Shital Yadav
