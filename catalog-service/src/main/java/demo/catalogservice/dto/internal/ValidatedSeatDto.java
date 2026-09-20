package demo.catalogservice.dto.internal;

import demo.catalogservice.enums.SeatType;

import java.math.BigDecimal;

/**
 * One validated seat within a {@link ShowSeatValidationResponse}. Note
 * booking-service's own mirror of this DTO ({@code demo.bookingservice.dtos.ValidatedSeatDto})
 * declares {@code seatType} as a plain {@code String} rather than importing
 * this enum — the two services don't share code, only a JSON contract, and
 * Jackson serializes an enum to its {@code name()} either way.
 */
public record ValidatedSeatDto(
        Long seatId,
        SeatType seatType,
        BigDecimal price
) {
}