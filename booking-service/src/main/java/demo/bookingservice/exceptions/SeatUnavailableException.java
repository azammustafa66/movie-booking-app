package demo.bookingservice.exceptions;

/**
 * Thrown when a seat can't be locked for a booking: it's already
 * {@code BOOKED}, currently {@code LOCKED} by an unexpired hold, or has no
 * {@link demo.bookingservice.entities.ShowSeat} row at all (only possible
 * during {@code releaseSeat} — {@code lockSeat} creates the row itself on
 * first touch rather than treating a missing row as an error). Mapped to
 * {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) {
        super(message);
    }
}
