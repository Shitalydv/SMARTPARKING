package SMARTPARKING;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginHandler {
    public boolean validateLogin(String username, String password) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";

        DatabaseHandler dbHandler = new DatabaseHandler();
        try (Connection conn = dbHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean usernameExists(String username) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";

        DatabaseHandler dbHandler = new DatabaseHandler();
        try (Connection conn = dbHandler.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isAdmin(String username) {
        String query = "SELECT is_admin FROM users WHERE username = ?";
    
        DatabaseHandler dbHandler = new DatabaseHandler();
        try (Connection conn = dbHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
    
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("is_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}