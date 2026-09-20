package demo.bookingservice.exceptions;

/**
 * Thrown when a booking (or one of its seats) exists and belongs to the
 * caller, but its current state rules out the requested action: cancelling
 * or confirming a booking that isn't {@code PENDING}/{@code CONFIRMED} as
 * required, confirming one whose hold already expired, or releasing a seat
 * that's locked for a different booking than the one releasing it.
 * Extends {@link IllegalStateException} since that's exactly what each of
 * these is — {@link demo.bookingservice.service.BookingService} throws this
 * instead of a raw {@code IllegalStateException} only so
 * {@link GlobalExceptionHandler} has a type of its own to map to
 * {@code 409 Conflict} rather than letting it fall through as a bare 500.
 */
public class InvalidBookingStateException extends IllegalStateException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
