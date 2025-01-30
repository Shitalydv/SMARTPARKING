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
    is_admin BOOLEAN DEFAULT FALSE
);

-- 2. Create the Vehicle table
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(50) UNIQUE,
    vehicle_type ENUM('bike', 'car'),
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Create the Hourly Rate table
CREATE TABLE hourly_rates (
    rate_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_type ENUM('bike', 'car') UNIQUE,
    hourly_rate DECIMAL(10, 2) NOT NULL
);

-- Insert hourly rates
INSERT INTO hourly_rates (vehicle_type, hourly_rate) VALUES 
('bike', 30.00),
('car', 50.00);

-- 4. Create the Parking Slots table
CREATE TABLE parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(10) NOT NULL UNIQUE,
    vehicle_type ENUM('bike', 'car') NOT NULL,
    is_occupied BOOLEAN DEFAULT FALSE,
    INDEX idx_slot_status (vehicle_type, is_occupied)
);

-- Insert bike slots (B001 to B100)
INSERT INTO parking_slots (slot_number, vehicle_type)
SELECT 
    CONCAT('B', LPAD(n.num, 3, '0')), 
    'bike'
FROM (
    SELECT @row := @row + 1 as num
    FROM (SELECT @row := 0) r,
         information_schema.columns
    LIMIT 100
) n;

-- Insert car slots (C001 to C050)
INSERT INTO parking_slots (slot_number, vehicle_type)
SELECT 
    CONCAT('C', LPAD(n.num, 3, '0')), 
    'car'
FROM (
    SELECT @row := @row + 1 as num
    FROM (SELECT @row := 0) r,
         information_schema.columns
    LIMIT 50
) n;

-- 5. Create the Parks table
CREATE TABLE parks (
    park_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT,
    parking_spot VARCHAR(10) NOT NULL,
    hourly_rate DECIMAL(10, 2),
    total_bill DECIMAL(10, 2),
    parked_datetime DATETIME DEFAULT CURRENT_TIMESTAMP,
    exit_time DATETIME DEFAULT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (parking_spot) REFERENCES parking_slots(slot_number),
    INDEX idx_parking_spot (parking_spot),
    INDEX idx_exit_time (exit_time)
);

-- Trigger to Mark Slot as Occupied When Parking
DELIMITER //
CREATE TRIGGER before_parking_insert
BEFORE INSERT ON parks
FOR EACH ROW
BEGIN
    UPDATE parking_slots 
    SET is_occupied = TRUE 
    WHERE slot_number = NEW.parking_spot;
END //
DELIMITER ;

-- Trigger to Free Slot When Vehicle Exits
DELIMITER //
CREATE TRIGGER after_parking_exit
AFTER UPDATE ON parks
FOR EACH ROW
BEGIN
    IF NEW.exit_time IS NOT NULL THEN
        UPDATE parking_slots 
        SET is_occupied = FALSE 
        WHERE slot_number = OLD.parking_spot;
    END IF;
END //
DELIMITER ;

-- Procedure to Assign Nearest Available Slot
DELIMITER //
CREATE PROCEDURE assign_nearest_slot(IN p_vehicle_type ENUM('bike', 'car'), OUT p_slot VARCHAR(10))
BEGIN
    SELECT slot_number INTO p_slot
    FROM parking_slots 
    WHERE vehicle_type = p_vehicle_type AND is_occupied = FALSE
    ORDER BY slot_number ASC
    LIMIT 1;
END //
DELIMITER ;

-- after adding dashboard 
-- final push
-- Nishan Bishwokarma
