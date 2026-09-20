package demo.bookingservice.dtos.internal;

import java.math.BigDecimal;

/**
 * One validated seat within a {@link ShowSeatValidationResponse}. Mirrors
 * catalog-service's own {@code ValidatedSeatDto} by JSON shape only, not
 * code — note {@code seatType} is a plain {@code String} here rather than
 * an enum, since Jackson serializes catalog-service's {@code SeatType} to
 * its name either way and this service has no use for the enum itself.
 */
public record ValidatedSeatDto(
        Long seatId,
        String seatType,
        BigDecimal price
) {}