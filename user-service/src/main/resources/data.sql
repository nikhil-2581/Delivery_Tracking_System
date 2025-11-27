-- Insert test users
-- Password for all users: "Password@123" (BCrypt hash)
-- You can generate BCrypt hash online or use: https://bcrypt-generator.com/

-- Admin user
INSERT INTO users (role, name, email, phone, hashed_password, status) VALUES
    ('ADMIN', 'Admin User', 'admin@delivery.com', '+1234567890',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE');

-- Customer users
INSERT INTO users (role, name, email, phone, hashed_password, status) VALUES
                                                                          ('CUSTOMER', 'John Doe', 'john.doe@example.com', '+1234567891',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE'),
                                                                          ('CUSTOMER', 'Jane Smith', 'jane.smith@example.com', '+1234567892',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE'),
                                                                          ('CUSTOMER', 'Bob Wilson', 'bob.wilson@example.com', '+1234567893',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE');

-- Driver users
INSERT INTO users (role, name, email, phone, hashed_password, status) VALUES
                                                                          ('DRIVER', 'Mike Driver', 'mike.driver@example.com', '+1234567894',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE'),
                                                                          ('DRIVER', 'Sarah Wheeler', 'sarah.wheeler@example.com', '+1234567895',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE'),
                                                                          ('DRIVER', 'Tom Racer', 'tom.racer@example.com', '+1234567896',
                                                                           '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW.2E7AYP3dm', 'ACTIVE');

-- Insert driver profiles
INSERT INTO drivers (user_id, license_no, vehicle_info, status, rating, total_deliveries) VALUES
                                                                                              (5, 'DL-001-2024', '{"make": "Toyota", "model": "Camry", "year": 2022, "plate": "ABC123"}', 'ONLINE', 4.8, 150),
                                                                                              (6, 'DL-002-2024', '{"make": "Honda", "model": "Civic", "year": 2023, "plate": "XYZ789"}', 'ONLINE', 4.9, 200),
                                                                                              (7, 'DL-003-2024', '{"make": "Ford", "model": "Focus", "year": 2021, "plate": "DEF456"}', 'OFFLINE', 4.7, 100);