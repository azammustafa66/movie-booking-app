CREATE TABLE bookings (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    show_id       BIGINT        NOT NULL,
    status        VARCHAR(50)   NOT NULL,
    total_amount  NUMERIC(12,2) NOT NULL,
    expires_at    TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);

CREATE INDEX idx_bookings_user_id ON bookings (user_id);
CREATE INDEX idx_bookings_show_id ON bookings (show_id);

CREATE TABLE booking_seats (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id BIGINT        NOT NULL REFERENCES bookings (id),
    seat_id    BIGINT        NOT NULL,
    show_id    BIGINT        NOT NULL,
    price      NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_booking_seats_booking_id ON booking_seats (booking_id);
CREATE INDEX idx_booking_seats_seat_id ON booking_seats (seat_id);
