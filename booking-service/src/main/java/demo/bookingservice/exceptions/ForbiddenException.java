package demo.bookingservice.exceptions;

/**
 * Thrown when an authenticated caller's role isn't allowed to perform the
 * requested action — e.g. a vendor attempting to book seats, which is a
 * customer/admin-only action. Mapped to {@code 403 Forbidden} by
 * {@link GlobalExceptionHandler}.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
