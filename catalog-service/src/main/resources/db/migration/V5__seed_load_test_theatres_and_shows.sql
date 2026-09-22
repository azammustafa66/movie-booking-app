-- Load-test seed data. Six more theatres, each with 2 screens of 300 seats
-- (150 REGULAR + 100 PREMIUM + 50 RECLINER, laid out as 12 rows x 25 seats:
-- A-B recliner, C-F premium, G-L regular), plus 6 shows to book against.
-- Dummy data for local/testing use only.

-- Fix pre-existing vendor_id mismatch: V3 backfilled ids 11/12/13, but the
-- vendors it references (karan.bhatia/meera.kapoor/sanjay.rao, seeded in
-- user-service V3__seed_additional_vendors.sql) actually land on ids 6/7/8
-- on a fresh database, since user-service now has its own DB and sequence.
UPDATE theatres SET vendor_id = 6 WHERE name = 'CineMax Downtown';
UPDATE theatres SET vendor_id = 7 WHERE name = 'Galaxy Cinemas';
UPDATE theatres SET vendor_id = 8 WHERE name = 'Metro Movie Hub';

-- New theatres (vendor_id references app_users seeded in user-service:
-- 5=vendor@example.com, 6=karan.bhatia, 7=meera.kapoor, 8=sanjay.rao)
INSERT INTO theatres (name, address, city, state, pincode, vendor_id) VALUES
    ('Sunrise Multiplex',    '221 FC Road',          'Pune',      'Maharashtra',  '411004', 5),
    ('Horizon Cinemas',      '88 Banjara Hills Road', 'Hyderabad', 'Telangana',    '500034', 6),
    ('Starlight Screens',    '14 Anna Salai',        'Chennai',   'Tamil Nadu',   '600002', 7),
    ('Grand Vista Theatres', '5 Park Street',        'Kolkata',   'West Bengal',  '700016', 8),
    ('Skyline Cinema Hub',   '32 SG Highway',        'Ahmedabad', 'Gujarat',      '380054', 5),
    ('Royal Screens',        '9 MI Road',            'Jaipur',    'Rajasthan',    '302001', 6);

-- Screens: 2 per new theatre, each sized to hold 300 seats
INSERT INTO screens (name, theatre_id, capacity, screen_type)
SELECT 'Screen 1', t.id, 300, v.screen_type
FROM theatres t
JOIN (VALUES
    ('Sunrise Multiplex',    'IMAX'),
    ('Horizon Cinemas',      'DOLBY'),
    ('Starlight Screens',    'FOUR_DX'),
    ('Grand Vista Theatres', 'THREE_D'),
    ('Skyline Cinema Hub',   'IMAX'),
    ('Royal Screens',        'DOLBY')
) AS v(theatre_name, screen_type) ON v.theatre_name = t.name
UNION ALL
SELECT 'Screen 2', t.id, 300, 'STANDARD'
FROM theatres t
WHERE t.name IN (
    'Sunrise Multiplex', 'Horizon Cinemas', 'Starlight Screens',
    'Grand Vista Theatres', 'Skyline Cinema Hub', 'Royal Screens'
);

-- Seats: 12 rows x 25 seats per screen = 300 (50 recliner / 100 premium / 150 regular)
INSERT INTO seats (row_label, seat_number, seat_type, price, screen_id)
SELECT layout.row_label, seat_num, layout.seat_type, layout.price, s.id
FROM screens s
JOIN theatres t ON t.id = s.theatre_id
CROSS JOIN (
    VALUES ('A', 'RECLINER', 400.00), ('B', 'RECLINER', 400.00),
           ('C', 'PREMIUM', 250.00), ('D', 'PREMIUM', 250.00),
           ('E', 'PREMIUM', 250.00), ('F', 'PREMIUM', 250.00),
           ('G', 'REGULAR', 150.00), ('H', 'REGULAR', 150.00),
           ('I', 'REGULAR', 150.00), ('J', 'REGULAR', 150.00),
           ('K', 'REGULAR', 150.00), ('L', 'REGULAR', 150.00)
) AS layout(row_label, seat_type, price)
CROSS JOIN generate_series(1, 25) AS seat_num
WHERE t.name IN (
    'Sunrise Multiplex', 'Horizon Cinemas', 'Starlight Screens',
    'Grand Vista Theatres', 'Skyline Cinema Hub', 'Royal Screens'
);

-- Shows: one per new theatre, spread across NOW_SHOWING/UPCOMING movies
INSERT INTO shows (movie_id, screen_id, start_time, end_time)
SELECT m.id, s.id, v.start_ts, v.start_ts + (m.duration_in_minutes || ' minutes')::interval
FROM (VALUES
    ('The Last Horizon',   'Sunrise Multiplex',    'Screen 1', TIMESTAMP '2026-09-23 15:00:00'),
    ('Beyond the Stars',   'Horizon Cinemas',      'Screen 1', TIMESTAMP '2026-09-23 18:30:00'),
    ('Midnight in Bombay', 'Starlight Screens',    'Screen 2', TIMESTAMP '2026-09-24 11:00:00'),
    ('Silent Verdict',     'Grand Vista Theatres', 'Screen 1', TIMESTAMP '2026-09-24 19:00:00'),
    ('The Quiet Storm',    'Skyline Cinema Hub',   'Screen 2', TIMESTAMP '2026-09-25 20:30:00'),
    ('Beyond the Stars',   'Royal Screens',        'Screen 1', TIMESTAMP '2026-09-25 16:00:00')
) AS v(movie_title, theatre_name, screen_name, start_ts)
JOIN movies m ON m.title = v.movie_title
JOIN theatres t ON t.name = v.theatre_name
JOIN screens s ON s.theatre_id = t.id AND s.name = v.screen_name;
