-- Food Ordering System Database Schema

-- Drop tables if they exist (in reverse order to avoid foreign key constraints)
DROP TABLE IF EXISTS order_promotions;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS menus;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS riders;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS customers;

-- Create tables

-- Customers table
CREATE TABLE customers (
       username VARCHAR(50) PRIMARY KEY,
       email VARCHAR(100) NOT NULL UNIQUE,
       phone VARCHAR(20) NOT NULL,
       first_name VARCHAR(50) NOT NULL,
       last_name VARCHAR(50) NOT NULL,
       password VARCHAR(255) NOT NULL,
       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
       created_at DATETIME NOT NULL DEFAULT GETDATE(),
       updated_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- Addresses table
CREATE TABLE addresses (
       id INT IDENTITY(1,1) PRIMARY KEY,
       street VARCHAR(255) NOT NULL,
       city VARCHAR(100) NOT NULL,
       state VARCHAR(100) NOT NULL,
       postal_code VARCHAR(20) NOT NULL,
       country VARCHAR(100) NOT NULL DEFAULT 'United States',
       latitude DECIMAL(10, 8),
       longitude DECIMAL(11, 8),
       customer_username VARCHAR(50),
       FOREIGN KEY (customer_username) REFERENCES customers(username) ON DELETE CASCADE
);


-- Restaurants table
CREATE TABLE restaurants (
     slug VARCHAR(100) PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     description TEXT,
     logo_url VARCHAR(255),
     phone VARCHAR(20) NOT NULL,
     email VARCHAR(100) NOT NULL,
     website VARCHAR(255),
     status VARCHAR(30) NOT NULL DEFAULT 'CLOSED' CHECK (status IN ('OPEN', 'CLOSED', 'TEMPORARILY_UNAVAILABLE')),
     opening_time TIME NOT NULL,
     closing_time TIME NOT NULL,
     address_id INT NOT NULL,
     created_at DATETIME NOT NULL DEFAULT GETDATE(),
     updated_at DATETIME NOT NULL DEFAULT GETDATE(),
     FOREIGN KEY (address_id) REFERENCES addresses(id)
);

-- Riders table
CREATE TABLE riders (
        id VARCHAR(50) PRIMARY KEY,
        first_name VARCHAR(50) NOT NULL,
        last_name VARCHAR(50) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        email VARCHAR(100) NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_DELIVERY', 'ON_BREAK')),
        date_of_birth DATE NOT NULL,
        license_number VARCHAR(50),
        license_expiry DATE,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL DEFAULT GETDATE()
);

-- Vehicles table
CREATE TABLE vehicles (
          registration_number VARCHAR(50) PRIMARY KEY,
          type VARCHAR(20) NOT NULL CHECK (type IN ('MOTORCYCLE', 'BICYCLE', 'CAR', 'SCOOTER', 'VAN')),
          make VARCHAR(50) NOT NULL,
          model VARCHAR(50) NOT NULL,
          year_of_manufacture INT NOT NULL,
          color VARCHAR(50) NOT NULL,
          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE')),
          insurance_expiry_date DATE NOT NULL,
          rider_id VARCHAR(50),
          created_at DATETIME NOT NULL DEFAULT GETDATE(),
          updated_at DATETIME NOT NULL DEFAULT GETDATE(),
          FOREIGN KEY (rider_id) REFERENCES riders(id) ON DELETE SET NULL
);

-- Menus table
CREATE TABLE menus (
       id INT IDENTITY(1,1) PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       description TEXT,
       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SEASONAL')),
       restaurant_slug VARCHAR(100) NOT NULL,
       created_at DATETIME NOT NULL DEFAULT GETDATE(),
       updated_at DATETIME NOT NULL DEFAULT GETDATE(),
       FOREIGN KEY (restaurant_slug) REFERENCES restaurants(slug) ON DELETE CASCADE
);

-- Menu Items table
CREATE TABLE menu_items (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price DECIMAL(10, 2) NOT NULL,
        image_url VARCHAR(255),
        category VARCHAR(20) NOT NULL CHECK (category IN ('APPETIZER', 'MAIN_COURSE', 'DESSERT', 'BEVERAGE', 'SIDE', 'SPECIAL')),
        is_available BIT NOT NULL DEFAULT 1,
        menu_id INT NOT NULL,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL DEFAULT GETDATE(),
        FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
);

-- Promotions table
CREATE TABLE promotions (
        code VARCHAR(50) PRIMARY KEY,
        description TEXT,
        type VARCHAR(20) NOT NULL CHECK (type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
        value DECIMAL(10, 2) NOT NULL,
        start_date DATETIME NOT NULL,
        end_date DATETIME NOT NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED')),
        restaurant_slug VARCHAR(100),
        usage_limit INT NOT NULL DEFAULT 0,
        usage_count INT NOT NULL DEFAULT 0,
        created_at DATETIME NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME NOT NULL DEFAULT GETDATE(),
        FOREIGN KEY (restaurant_slug) REFERENCES restaurants(slug) ON DELETE SET NULL
);

-- Create index for promotions
CREATE INDEX idx_promotion_status_dates ON promotions(status, start_date, end_date);

-- Orders table
CREATE TABLE orders (
    order_code VARCHAR(50) PRIMARY KEY,
    customer_username VARCHAR(50) NOT NULL,
    restaurant_slug VARCHAR(100) NOT NULL,
    address_id INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' 
        CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED')),
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    special_instructions TEXT,
    rider_id VARCHAR(50),
    placed_at DATETIME NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME NOT NULL DEFAULT GETDATE(),

    FOREIGN KEY (customer_username) REFERENCES customers(username),
    FOREIGN KEY (restaurant_slug) REFERENCES restaurants(slug),
    FOREIGN KEY (rider_id) REFERENCES riders(id) ON DELETE SET NULL,
    FOREIGN KEY (address_id) REFERENCES addresses(id)  -- No ON DELETE action = RESTRICT-like behavior
);


-- Order Items table
CREATE TABLE order_items (
         id INT IDENTITY(1,1) PRIMARY KEY,
         order_code VARCHAR(50) NOT NULL,
         menu_item_id INT NOT NULL,
         menu_item_name VARCHAR(255) NOT NULL,
         quantity INT NOT NULL,
         price_at_order_time DECIMAL(10, 2) NOT NULL,
         special_instructions TEXT,
         FOREIGN KEY (order_code) REFERENCES orders(order_code) ON DELETE CASCADE,
         FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Payments table
CREATE TABLE payments (
          id INT IDENTITY(1,1) PRIMARY KEY,
          order_code VARCHAR(50) NOT NULL,
          amount DECIMAL(10, 2) NOT NULL,
          payment_status VARCHAR(20) NOT NULL DEFAULT 'INITIATED' CHECK (payment_status IN ('INITIATED', 'PROCESSING', 'PAID', 'FAILED', 'REFUNDED')),
          payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH' CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'CASH', 'MOBILE_PAYMENT', 'ONLINE_BANKING')),
          transaction_id VARCHAR(255),
          created_at DATETIME NOT NULL DEFAULT GETDATE(),
          updated_at DATETIME NOT NULL DEFAULT GETDATE(),
          FOREIGN KEY (order_code) REFERENCES orders(order_code) ON DELETE CASCADE
);

-- Order Promotions junction table
CREATE TABLE order_promotions (
          order_code VARCHAR(50) NOT NULL,
          promotion_code VARCHAR(50) NOT NULL,
          created_at DATETIME NOT NULL DEFAULT GETDATE(),
          PRIMARY KEY (order_code, promotion_code),
          FOREIGN KEY (order_code) REFERENCES orders(order_code) ON DELETE CASCADE,
          FOREIGN KEY (promotion_code) REFERENCES promotions(code) ON DELETE CASCADE
);

-- Sample data for testing
-- Insert customers
INSERT INTO customers (username, email, phone, first_name, last_name, password, status)
VALUES
    ('johndoe', 'john@example.com', '555-123-4567', 'John', 'Doe', 'password123', 'ACTIVE'),
    ('janedoe', 'jane@example.com', '555-987-6543', 'Jane', 'Doe', 'password456', 'ACTIVE'),
    ('bobsmith', 'bob@example.com', '555-555-5555', 'Bob', 'Smith', 'password789', 'INACTIVE');

-- Insert addresses
INSERT INTO addresses (street, city, state, postal_code, country, latitude, longitude, customer_username)
VALUES
    ('123 Main St', 'New York', 'NY', '10001', 'United States', 40.7128, -74.0060, 'johndoe'),
    ('456 Elm St', 'Los Angeles', 'CA', '90001', 'United States', 34.0522, -118.2437, 'janedoe'),
    ('789 Oak St', 'Chicago', 'IL', '60601', 'United States', 41.8781, -87.6298, 'bobsmith');

-- Insert an address for a restaurant
INSERT INTO addresses (street, city, state, postal_code, country, latitude, longitude)
VALUES
    ('100 Broadway', 'New York', 'NY', '10001', 'United States', 40.7112, -74.0055);

-- Insert a restaurant
INSERT INTO restaurants (slug, name, description, logo_url, phone, email, website, status, opening_time, closing_time, address_id)
VALUES
    ('nyc-pizza', 'NYC Pizza', 'Authentic New York-style pizza', 'https://example.com/logo.png', '555-111-2222', 'info@nycpizza.com', 'https://nycpizza.com', 'OPEN', '11:00:00', '23:00:00', 4);

-- Insert a menu
INSERT INTO menus (name, description, status, restaurant_slug)
VALUES
    ('Main Menu', 'Our regular menu available every day', 'ACTIVE', 'nyc-pizza');

-- Insert menu items
INSERT INTO menu_items (name, description, price, image_url, category, is_available, menu_id)
VALUES
    ('Margherita Pizza', 'Classic pizza with tomato sauce, mozzarella, and basil', 12.99, 'https://example.com/margherita.jpg', 'MAIN_COURSE', 1, 1),
    ('Pepperoni Pizza', 'Pizza with tomato sauce, mozzarella, and pepperoni', 14.99, 'https://example.com/pepperoni.jpg', 'MAIN_COURSE', 1, 1),
    ('Caesar Salad', 'Romaine lettuce with Caesar dressing and croutons', 8.99, 'https://example.com/caesar.jpg', 'APPETIZER', 1, 1),
    ('Soda', 'Choice of Coke, Sprite, or Fanta', 2.99, 'https://example.com/soda.jpg', 'BEVERAGE', 1, 1);

-- Insert a rider
INSERT INTO riders (id, first_name, last_name, phone, email, status, date_of_birth, license_number, license_expiry)
VALUES
    ('RDR001', 'Mike', 'Johnson', '555-444-3333', 'mike@example.com', 'ACTIVE', '1990-05-15', 'DL123456', '2025-05-15');

-- Insert a vehicle
INSERT INTO vehicles (registration_number, type, make, model, year_of_manufacture, color, status, insurance_expiry_date, rider_id)
VALUES
    ('NY12345', 'MOTORCYCLE', 'Honda', 'CBR150', 2020, 'Red', 'ACTIVE', '2024-12-31', 'RDR001');

-- Insert a promotion
INSERT INTO promotions (code, description, type, value, start_date, end_date, status, restaurant_slug, usage_limit)
VALUES
    ('WELCOME20', '20% off your first order', 'PERCENTAGE', 20.00, '2023-01-01 00:00:00', '2023-12-31 23:59:59', 'ACTIVE', 'nyc-pizza', 0);
