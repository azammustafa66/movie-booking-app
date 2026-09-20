package demo.bookingservice.exceptions;

/**
 * Thrown when a booking lookup finds no matching row for the caller —
 * either the id doesn't exist at all, or it exists but belongs to a
 * different user (deliberately indistinguishable from the caller's
 * perspective, so one customer can't probe another's booking ids). Mapped
 * to {@code 404 Not Found} by {@link GlobalExceptionHandler}.
 */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
