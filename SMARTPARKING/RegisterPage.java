package SMARTPARKING;

import java.awt.*;
import javax.swing.*;

public class RegisterPage extends JPanel {
    private JFrame frame;
    private Container container;
    private JTextField fullnamefield;
    private JTextField usernamefield;
    private JTextField email_addressfield;
    private JPasswordField passwordfield;
    private JCheckBox adminCheckBox;

    public RegisterPage(JFrame frame) {
        this.frame = frame;
        setupContainer();
        addRegisterImage();
        addHeadingLabels();
        addInputFields();
        addPasswordToggle();
        addAdminCheckBox();
        addNavigationButtons();
    }

    private void setupContainer() {
        container = this;
        container.setLayout(null);
        container.setBackground(Color.decode("#cee6e6")); // Set background color to #cee6e6
    }

    private void addRegisterImage() {
        ImageIcon originalIcon = new ImageIcon("C:\\Users\\ACER\\Desktop\\shital_smartparking\\SMARTPARKING\\logo.png");
        Image originalImage = originalIcon.getImage();

        int desiredWidth = 400;
        int desiredHeight = 400;

        Image resizedImage = originalImage.getScaledInstance(desiredWidth, desiredHeight, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        JLabel imageLabel = new JLabel(resizedIcon);
        imageLabel.setBounds(50, 50, desiredWidth, desiredHeight);
        container.add(imageLabel);
    }

    private void addHeadingLabels() {
        JLabel registerLabel = new JLabel("Welcome to Smart Parking");
        registerLabel.setForeground(new Color(35, 54, 75)); // Set label color to #23364b
        container.add(registerLabel);
        registerLabel.setBounds(580, 30, 400, 80);
        registerLabel.setFont(new Font("Arial", Font.BOLD, 30));
    }

    private void addInputFields() {
        addFormField("Full Name", 80, fullnamefield = new JTextField());
        addFormField("Username", 160, usernamefield = new JTextField());
        addFormField("Email Address", 230, email_addressfield = new JTextField());
        addFormField("Password:", 300, passwordfield = new JPasswordField());
    }

    private void addFormField(String labelText, int yPosition, JTextField field) {
        JLabel label = new JLabel(labelText);
        container.add(label);
        label.setBounds(550, yPosition, 200, 80);
        label.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 20));
        label.setForeground(new Color(35, 54, 75)); // Set label color to #23364b

        field.setBounds(550, yPosition + 60, 300, 30);
        container.add(field);
        field.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 17));
    }

    private void addPasswordToggle() {
        JButton showPassword = new JButton("Show");
        showPassword.setBounds(862, 360, 70, 25);
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

    private void addAdminCheckBox() {
        adminCheckBox = new JCheckBox("Admin ?");
        adminCheckBox.setBounds(550, 400, 300, 30);
        adminCheckBox.setBackground(new Color(240, 240, 240));
        adminCheckBox.setForeground(new Color(35, 54, 75)); // Set label color to #23364b

        container.add(adminCheckBox);
    }

    private void addNavigationButtons() {
        JButton backButton = new JButton("Login");
        backButton.setBounds(550, 450, 100, 30);
        container.add(backButton);
        backButton.addActionListener(e -> {
            SmartParkingApp.showLoginPage();
        });

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(700, 450, 100, 30);
        container.add(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
    }

    private void handleRegistration() {
        String fullname = fullnamefield.getText().trim();
        String username = usernamefield.getText().trim();
        String email_address = email_addressfield.getText().trim();
        String password = new String(passwordfield.getPassword()).trim();
        boolean isAdmin = adminCheckBox.isSelected();

        if (fullname.isEmpty() || username.isEmpty() || email_address.isEmpty() || password.isEmpty()) {
            showError("All fields must be filled out.", null);
            return;
        }

        if (!isValidEmail(email_address)) {
            showError("Invalid email address.", email_addressfield);
            return;
        }

        if (!isStrongPassword(password)) {
            showError("Password must be at least 8 characters long and include letters, numbers, and special characters.", passwordfield);
            return;
        }

        RegistrationHandler registrationHandler = new RegistrationHandler();
        if (registrationHandler.usernameExists(username)) {
            showError("Username already exists.", usernamefield);
            return;
        }

        boolean success = registrationHandler.registerUser(fullname, username, email_address, password, isAdmin);

        if (success) {
            JOptionPane.showMessageDialog(frame, "Registration successful!");
        } else {
            showError("Registration failed.", null);
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Za-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*()-+=<>?].*");
    }

    
    private void showError(String message, JComponent component) {
        JOptionPane.showMessageDialog(frame, message);
        if (component != null) {
            component.requestFocus();
        }
    }
}

// Rahul Kumar Sharma
// final done 