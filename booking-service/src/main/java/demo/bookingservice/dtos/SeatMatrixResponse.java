package demo.bookingservice.dtos;

import java.util.List;

/** The full seat map for a show, returned by {@code GET /api/v1/bookings/shows/{showId}/seats}. */
public record SeatMatrixResponse(
        Long showId,
        Long screenId,
        List<SeatMatrixDto> seats
) {}