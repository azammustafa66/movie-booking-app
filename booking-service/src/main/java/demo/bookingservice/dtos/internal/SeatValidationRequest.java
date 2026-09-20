package demo.bookingservice.dtos.internal;

import java.util.List;

/** The seat ids to validate, sent to catalog-service's {@code POST /internal/catalog/shows/{showId}/validate-seats}. */
public record SeatValidationRequest(
        List<Long> seatIds
) {}