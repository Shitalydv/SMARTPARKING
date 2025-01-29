package SMARTPARKING;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class LoginPage extends JPanel {
    private JFrame frame;
    private Container container;
    private JTextField usernamefield;
    private JPasswordField passwordfield;

    public LoginPage(JFrame frame) {
        this.frame = frame;
        setupContainer();
        addHeadingLabels();
        addInputFields();
        addPasswordToggle();
        addNavigationButtons();
        addLoginImage();
    }

    private void setupContainer() {
        container = this;
        container.setLayout(null);
        container.setBackground(Color.decode("#cee6e6"));
    }

    private void addHeadingLabels() {
        JLabel loginLabel = new JLabel("Hi ! Smart Parking");
        loginLabel.setForeground(new Color(35, 54, 75)); // Set label color to #23364b
        container.add(loginLabel);
        loginLabel.setBounds(80, 30, 400, 80);
        loginLabel.setFont(new Font("Arial", Font.BOLD, 30));
    }

    private void addInputFields() {
        addFormField("Username", 160, usernamefield = new JTextField());
        addFormField("Password", 238, passwordfield = new JPasswordField());
    }

    private void addFormField(String labelText, int yPosition, JTextField field) {
        JLabel label = new JLabel(labelText);
        container.add(label);
        label.setBounds(80, yPosition, 200, 80);
        label.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 20));
        label.setForeground(new Color(35, 54, 75)); // Set label color to #23364b

        field.setBounds(80, yPosition + 60, 300, 30);
        field.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 17));
        container.add(field);
    }

    private void addPasswordToggle() {
        JButton showPassword = new JButton("Show");
        showPassword.setBounds(392, 298, 70, 25);
        container.add(showPassword);

        showPassword.addActionListener(e -> {
            if (showPassword.getText().equals("Show")) {
                passwordfield.setEchoChar((char) 0);
                showPassword.setText("Hide");
            } else {
                passwordfield.setEchoChar('*');
                showPassword.setText("Show");
            }
        });
    }

    private void addNavigationButtons() {
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(80, 360, 100, 30);
        container.add(registerButton);
        registerButton.addActionListener(e -> {
            SmartParkingApp.showSignupPage();
        });

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(230, 360, 100, 30);
        container.add(loginButton);
        loginButton.addActionListener(e -> handleLogin());
    }

    private void addLoginImage() {
        // Load original image
        ImageIcon originalIcon = new ImageIcon("SMARTPARKING/logo.png");
        
        // Convert to BufferedImage
        BufferedImage bufferedImage = new BufferedImage(
            originalIcon.getIconWidth(),
            originalIcon.getIconHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = bufferedImage.createGraphics();
        originalIcon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
    
        // Resize image
        int desiredWidth = 400;
        int desiredHeight = 400;
        Image resizedImage = bufferedImage.getScaledInstance(desiredWidth, desiredHeight, Image.SCALE_SMOOTH);
        
        // Create label and add to container
        JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
        imageLabel.setBounds(500, 50, desiredWidth, desiredHeight);
        container.add(imageLabel);
    }

    private void handleLogin() {
        String username = usernamefield.getText().trim();
        String password = new String(passwordfield.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("All fields must be filled out.", null);
            return;
        }
        

        
        LoginHandler loginHandler = new LoginHandler();
        if (loginHandler.usernameExists(username)) {
            boolean success = loginHandler.validateLogin(username, password);
            if (success) {
                // Navigate to dashboard
                SmartParkingApp.showDashboard(username);  // Changed to use FitnessTrackerApp navigation
                JOptionPane.showMessageDialog(frame, "Login successful!");
    
            } else {
                showError("Invalid password.", null);
            }
        }else{
            showError("username dosenot exists.", null);
            return;
        }
        


        
    }

    private void showError(String message, JComponent component) {
        JOptionPane.showMessageDialog(frame, message);
        if (component != null) {
            component.requestFocus();
        }
    }
}

// Rahul kumar Sharma
// final done

