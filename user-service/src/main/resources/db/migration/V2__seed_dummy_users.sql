-- Dummy data for local/testing use only.
-- All accounts share the password: Password123!  (bcrypt hash below)
INSERT INTO app_users (first_name, last_name, email, password, role, created_at, updated_at)
VALUES
    ('Ava', 'Sharma', 'ava.sharma@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'CUSTOMER', now(), now()),
    ('Rohan', 'Mehta', 'rohan.mehta@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'CUSTOMER', now(), now()),
    ('Priya', 'Nair', 'priya.nair@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'CUSTOMER', now(), now()),
    ('Admin', 'User', 'admin@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'ADMIN', now(), now()),
    ('Vendor', 'Ops', 'vendor@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'VENDOR', now(), now());
