package SMARTPARKING;

import java.sql.*;

public class UserProfile {
    private int userId;
    private String fullname;
    private String username;
    private String email;
    private String password;
    private boolean isAdmin;

    public UserProfile(String username) {
        this.username = username;
        fetchUserDetails(username);
    }

    private void fetchUserDetails(String username) {
        String query = "SELECT user_id, fullname, username, email, password, is_admin FROM users WHERE username = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                this.userId = rs.getInt("user_id");
                this.fullname = rs.getString("fullname");
                this.username = rs.getString("username");
                this.email = rs.getString("email");
                this.password = rs.getString("password");
                this.isAdmin = rs.getBoolean("is_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateProfile(String fullname, String email, String password) {
        String query = "UPDATE users SET fullname = ?, email = ?, password = ? WHERE user_id = ?";
        
        try (Connection conn = new DatabaseHandler().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, fullname);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setInt(4, this.userId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                this.fullname = fullname;
                this.email = email;
                this.password = password;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFullname() { return fullname; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isAdmin() { return isAdmin; }
}

