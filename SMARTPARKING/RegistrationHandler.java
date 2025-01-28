
package SMARTPARKING;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationHandler {
    public boolean registerUser(String fullname, String username, String email, String password, boolean isAdmin) {
        if (usernameExists(username)) {
            return false;
        }

        String insertQuery = "INSERT INTO users (fullname, username, email, password, is_admin) VALUES (?, ?, ?, ?, ?)";

        DatabaseHandler dbHandler = new DatabaseHandler();
        try (Connection conn = dbHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setBoolean(5, isAdmin);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
}