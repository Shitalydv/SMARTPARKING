-- Create the database
CREATE DATABASE smartparking;

-- Use the database
USE smartparking;


-- 1. Create the Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    fullname VARCHAR(255),
    username VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE -- Differentiates users and admins
);


-- 2. Create the Vehicle table
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(50) UNIQUE, -- Unique vehicle number
    vehicle_type ENUM('bike', 'car'), -- Only two types: bike or car
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE -- Links vehicle to a user
);


-- 3. Create the Hourly Rate table
CREATE TABLE hourly_rates (
    rate_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type ENUM('bike', 'car') UNIQUE, -- Only two types: bike or car
    hourly_rate DECIMAL(10, 2) NOT NULL -- Stores the hourly rate
);


-- Insert hourly rates
INSERT INTO hourly_rates (vehicle_type, hourly_rate) VALUES 
('bike', 30.00),
('car', 50.00);

-- 4. Create the Parks table
CREATE TABLE parks (
    park_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT,
    hourly_rate DECIMAL(10, 2),
    total_bill DECIMAL(10, 2),
    parked_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE -- Links park to a vehicle
);


-- 5. Create trigger to auto-register as admin or user
DELIMITER //
CREATE TRIGGER after_user_insert
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    IF NEW.is_admin = FALSE THEN
        -- Standard users don't need any special registration handling
        INSERT INTO vehicles (vehicle_number, vehicle_type, user_id) VALUES
        ('', 'bike', NEW.user_id); -- Placeholder vehicle entry for users
    END IF;
END //
DELIMITER ;


-- final done  
-- Nisahn Bishwokarma
