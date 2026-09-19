-- Dummy data for local/testing use only.

-- Genres
INSERT INTO genres (name) VALUES
    ('Action'), ('Drama'), ('Comedy'), ('Sci-Fi'), ('Thriller'), ('Romance');

-- Movies
INSERT INTO movies (title, description, language, release_date, certification, duration_in_minutes, poster_url, status)
VALUES
    ('The Last Horizon', 'A crew races against time to save Earth from a rogue asteroid.', 'English', '2026-03-14', 'UA', 142, 'https://picsum.photos/seed/movie1/400/600', 'UPCOMING'),
    ('Midnight in Bombay', 'A romance blossoms across two decades in the streets of Bombay.', 'Hindi', '2026-01-20', 'U', 128, 'https://picsum.photos/seed/movie2/400/600', 'NOW_SHOWING'),
    ('Silent Verdict', 'A gripping courtroom thriller about a case that shakes a small town.', 'English', '2025-11-05', 'A', 118, 'https://picsum.photos/seed/movie3/400/600', 'NOW_SHOWING'),
    ('Chai and Chaos', 'A comedy of errors set in a family-run tea stall.', 'Hindi', '2025-09-01', 'U', 105, 'https://picsum.photos/seed/movie4/400/600', 'ENDED'),
    ('Beyond the Stars', 'Humanity''s first interstellar colony faces an unknown threat.', 'English', '2026-06-10', 'UA', 150, 'https://picsum.photos/seed/movie5/400/600', 'UPCOMING'),
    ('The Quiet Storm', 'A retired detective is pulled into one last dangerous case.', 'English', '2025-12-12', 'A', 132, 'https://picsum.photos/seed/movie6/400/600', 'NOW_SHOWING');

-- Movie <-> Genre associations
INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id
FROM movies m, genres g
WHERE (m.title = 'The Last Horizon' AND g.name IN ('Action', 'Sci-Fi'))
   OR (m.title = 'Midnight in Bombay' AND g.name IN ('Romance', 'Drama'))
   OR (m.title = 'Silent Verdict' AND g.name IN ('Thriller', 'Drama'))
   OR (m.title = 'Chai and Chaos' AND g.name IN ('Comedy'))
   OR (m.title = 'Beyond the Stars' AND g.name IN ('Sci-Fi', 'Action'))
   OR (m.title = 'The Quiet Storm' AND g.name IN ('Thriller', 'Action'));

-- Theatres
INSERT INTO theatres (name, address, city, state, pincode) VALUES
    ('CineMax Downtown', '12 MG Road', 'Bengaluru', 'Karnataka', '560001'),
    ('Galaxy Cinemas', '45 Marine Drive', 'Mumbai', 'Maharashtra', '400002'),
    ('Metro Movie Hub', '78 Connaught Place', 'New Delhi', 'Delhi', '110001');

-- Screens (capacity matches the 6 rows x 10 seats generated below)
INSERT INTO screens (name, theatre_id, capacity, screen_type)
SELECT 'Screen 1', t.id, 60, 'IMAX' FROM theatres t WHERE t.name = 'CineMax Downtown'
UNION ALL
SELECT 'Screen 2', t.id, 60, 'STANDARD' FROM theatres t WHERE t.name = 'CineMax Downtown'
UNION ALL
SELECT 'Screen 1', t.id, 60, 'DOLBY' FROM theatres t WHERE t.name = 'Galaxy Cinemas'
UNION ALL
SELECT 'Screen 2', t.id, 60, 'STANDARD' FROM theatres t WHERE t.name = 'Galaxy Cinemas'
UNION ALL
SELECT 'Screen 1', t.id, 60, 'FOUR_DX' FROM theatres t WHERE t.name = 'Metro Movie Hub';

-- Seats: rows A-B recliner, C-D premium, E-F regular; 10 seats per row per screen
INSERT INTO seats (row_label, seat_number, seat_type, screen_id)
SELECT layout.row_label, seat_num, layout.seat_type, s.id
FROM screens s
CROSS JOIN (
    VALUES ('A', 'RECLINER'), ('B', 'RECLINER'),
           ('C', 'PREMIUM'), ('D', 'PREMIUM'),
           ('E', 'REGULAR'), ('F', 'REGULAR')
) AS layout(row_label, seat_type)
CROSS JOIN generate_series(1, 10) AS seat_num;

-- Shows for movies currently in theatres
INSERT INTO shows (movie_id, screen_id, start_time, end_time)
SELECT m.id, s.id, v.start_ts, v.start_ts + (m.duration_in_minutes || ' minutes')::interval
FROM (VALUES
    ('Midnight in Bombay', 'CineMax Downtown', 'Screen 1', TIMESTAMP '2026-09-20 10:00:00'),
    ('Midnight in Bombay', 'Galaxy Cinemas',   'Screen 2', TIMESTAMP '2026-09-20 14:30:00'),
    ('Silent Verdict',     'CineMax Downtown', 'Screen 2', TIMESTAMP '2026-09-20 18:00:00'),
    ('Silent Verdict',     'Metro Movie Hub',  'Screen 1', TIMESTAMP '2026-09-21 11:00:00'),
    ('The Quiet Storm',    'Galaxy Cinemas',   'Screen 1', TIMESTAMP '2026-09-21 20:00:00'),
    ('The Quiet Storm',    'Metro Movie Hub',  'Screen 1', TIMESTAMP '2026-09-22 21:00:00')
) AS v(movie_title, theatre_name, screen_name, start_ts)
JOIN movies m ON m.title = v.movie_title
JOIN theatres t ON t.name = v.theatre_name
JOIN screens s ON s.theatre_id = t.id AND s.name = v.screen_name;
