CREATE TABLE show_seats (
    id           BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    show_id      BIGINT      NOT NULL,
    seat_id      BIGINT      NOT NULL,
    status       VARCHAR(50) NOT NULL,
    booking_id   BIGINT,
    locked_until TIMESTAMP,
    UNIQUE (show_id, seat_id)
);

CREATE INDEX idx_show_seats_show_id ON show_seats (show_id);
CREATE INDEX idx_show_seats_booking_id ON show_seats (booking_id);
