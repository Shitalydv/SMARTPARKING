package SMARTPARKING;

import java.sql.*;
import java.util.*;

public class ParkingManager {
    private int userId;

    public ParkingManager(int userId) {
        this.userId = userId;
    }

    public boolean isVehicleParked(int vehicleId) {
        String query = "SELECT COUNT(*) FROM parks WHERE vehicle_id = ? AND exit_time IS NULL";
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, vehicleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String parkVehicle(int vehicleId, int hours) {
        // Check if vehicle is already parked
        if (isVehicleParked(vehicleId)) {
            return null;
        }

        String getTypeQuery = "SELECT vehicle_type FROM vehicles WHERE vehicle_id = ?";
        String parkQuery = "INSERT INTO parks (vehicle_id, parking_spot, hourly_rate, total_bill) VALUES (?, ?, ?, ?)";
        String findSlotQuery = "{CALL assign_nearest_slot(?, ?)}";
        String updateSlotQuery = "UPDATE parking_slots SET is_occupied = TRUE WHERE slot_number = ?";
    
        try (Connection conn = new DatabaseHandler().getConnection()) {
            conn.setAutoCommit(false);
    
            try {
                // Get vehicle type
                String vehicleType;
                try (PreparedStatement pstmt = conn.prepareStatement(getTypeQuery)) {
                    pstmt.setInt(1, vehicleId);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) return null;
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
                double rate = calculateHourlyRate(vehicleType);
                double total = rate * hours;
    
                // Park vehicle
                try (PreparedStatement pstmt = conn.prepareStatement(parkQuery)) {
                    pstmt.setInt(1, vehicleId);
                    pstmt.setString(2, slotNumber);
                    pstmt.setDouble(3, rate);
                    pstmt.setDouble(4, total);
                    pstmt.executeUpdate();
                }

                // Update slot status
                try (PreparedStatement pstmt = conn.prepareStatement(updateSlotQuery)) {
                    pstmt.setString(1, slotNumber);
                    pstmt.executeUpdate();
                }
    
                conn.commit();
                return slotNumber;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ParkingRecord> getParkingHistory() {
        List<ParkingRecord> history = new ArrayList<>();
        String query = """
            SELECT v.vehicle_number, p.parked_datetime, p.total_bill 
            FROM parks p 
            JOIN vehicles v ON p.vehicle_id = v.vehicle_id 
            WHERE v.user_id = ? 
            ORDER BY 
                CASE WHEN p.exit_time IS NULL THEN 1 ELSE 0 END,
                p.parked_datetime DESC
        """;
    
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
        String query = "SELECT vehicle_type FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, vehicleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String vehicleType = rs.getString("vehicle_type");
                return calculateHourlyRate(vehicleType) * hours;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalExpenses() {
        String query = """
            SELECT COALESCE(SUM(total_bill), 0.0) as total 
            FROM parks p 
            JOIN vehicles v ON p.vehicle_id = v.vehicle_id 
            WHERE v.user_id = ?
        """;
        
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


    public boolean exitVehicle(int vehicleId) {
        String updateParkQuery = "UPDATE parks SET exit_time = CURRENT_TIMESTAMP WHERE vehicle_id = ? AND exit_time IS NULL";
        String updateSlotQuery = "UPDATE parking_slots SET is_occupied = FALSE WHERE slot_number = (SELECT parking_spot FROM parks WHERE vehicle_id = ? AND exit_time IS NULL)";
        
        try (Connection conn = new DatabaseHandler().getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update slot status
                try (PreparedStatement pstmt = conn.prepareStatement(updateSlotQuery)) {
                    pstmt.setInt(1, vehicleId);
                    pstmt.executeUpdate();
                }

                // Update park record
                try (PreparedStatement pstmt = conn.prepareStatement(updateParkQuery)) {
                    pstmt.setInt(1, vehicleId);
                    boolean success = pstmt.executeUpdate() > 0;
                    if (success) {
                        conn.commit();
                        return true;
                    }
                }
                
                conn.rollback();
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private double calculateHourlyRate(String vehicleType) {
        return vehicleType.equals("bike") ? 30.0 : 50.0;
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
