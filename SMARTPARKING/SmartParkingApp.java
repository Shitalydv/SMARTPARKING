package SMARTPARKING;

import javax.swing.*;

public class SmartParkingApp {
    private static JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("Smart Parking");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1000, 600);
            mainFrame.setLocationRelativeTo(null);
            showLoginPage();
            // showDashboard("shital");
        });
    }

    public static void showSignupPage() {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(new RegisterPage(mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
    }

    public static void showLoginPage() {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(new LoginPage(mainFrame));
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
    }

    public static void showDashboard(String username) {
        LoginHandler loginHandler = new LoginHandler();
        boolean isTrainer = loginHandler.isAdmin(username);
        
        mainFrame.getContentPane().removeAll();
        if (isTrainer) {
            mainFrame.add(new AdminDashboard(username));
        } else {
            mainFrame.add(new UserDashboard(username));
        }
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
    }
}