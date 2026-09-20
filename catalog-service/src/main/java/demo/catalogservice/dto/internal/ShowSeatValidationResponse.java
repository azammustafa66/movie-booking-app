package demo.catalogservice.dto.internal;

import java.util.List;

/**
 * Confirms every requested seat id belongs to {@code showId}'s screen, with
 * each seat's type so booking-service can price the booking. {@code seats}
 * is always the same size as the request's {@code seatIds} — a mismatch
 * fails the whole call ({@link demo.catalogservice.exceptions.InvalidRequestException})
 * rather than returning a partial list.
 */
public record ShowSeatValidationResponse(
        Long showId,
        Long screenId,
        List<ValidatedSeatDto> seats
) {
}