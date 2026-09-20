package demo.bookingservice.dtos.internal;

import demo.bookingservice.enums.SeatType;

import java.math.BigDecimal;

/**
 * One seat within a {@link CatalogShowSeatMatrixResponse}. Booking-service's
 * mirror of catalog-service's own {@code CatalogSeatDto} — the two share a
 * JSON contract via {@link demo.bookingservice.clients.CatalogClient}, not
 * code, matching how {@link ValidatedSeatDto} mirrors catalog-service's
 * equivalent for {@code validateSeats}.
 */
public record CatalogSeatDto(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        BigDecimal price
) {}