CREATE TABLE genres (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE movies (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title                VARCHAR(255) NOT NULL,
    description          TEXT         NOT NULL,
    language             VARCHAR(255) NOT NULL,
    release_date         DATE         NOT NULL,
    certification        VARCHAR(50),
    duration_in_minutes  INTEGER,
    poster_url           VARCHAR(500),
    status               VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_movies_status ON movies (status);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies (id),
    genre_id BIGINT NOT NULL REFERENCES genres (id),
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE theatres (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city    VARCHAR(255) NOT NULL,
    state   VARCHAR(255) NOT NULL,
    pincode VARCHAR(255) NOT NULL
);

CREATE INDEX idx_theatres_city ON theatres (city);

CREATE TABLE screens (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    theatre_id  BIGINT       NOT NULL REFERENCES theatres (id),
    capacity    INTEGER      NOT NULL,
    screen_type VARCHAR(50)  NOT NULL
);

CREATE INDEX idx_screens_theatre_id ON screens (theatre_id);

CREATE TABLE seats (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    row_label   VARCHAR(255) NOT NULL,
    seat_number INTEGER      NOT NULL,
    seat_type   VARCHAR(50)  NOT NULL,
    screen_id   BIGINT       NOT NULL REFERENCES screens (id),
    UNIQUE (screen_id, row_label, seat_number)
);

CREATE TABLE shows (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    movie_id   BIGINT    NOT NULL REFERENCES movies (id),
    screen_id  BIGINT    NOT NULL REFERENCES screens (id),
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL
);

CREATE INDEX idx_shows_movie_id ON shows (movie_id);
CREATE INDEX idx_shows_screen_id ON shows (screen_id);
CREATE INDEX idx_shows_start_time ON shows (start_time);
