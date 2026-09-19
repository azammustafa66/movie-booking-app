-- Dummy data for local/testing use only.
-- One vendor per theatre chain seeded in catalog-service (see catalog-service V2/V3 migrations).
-- Shares the same test password as V2: Password123!
INSERT INTO app_users (first_name, last_name, email, password, role, created_at, updated_at)
VALUES
    ('Karan', 'Bhatia', 'karan.bhatia@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'VENDOR', now(), now()),
    ('Meera', 'Kapoor', 'meera.kapoor@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'VENDOR', now(), now()),
    ('Sanjay', 'Rao', 'sanjay.rao@example.com', '$2b$12$wZgL6SkCEI/vMno/KBJytOY5NTava7IRjg2jps/pI5xuUw8ln5qx', 'VENDOR', now(), now());
