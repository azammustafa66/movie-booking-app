package demo.catalogservice.exceptions;

/**
 * Thrown when an authenticated caller — typically a vendor — attempts to
 * read or modify a resource they don't own (a theatre, screen, seat, or show
 * outside their own {@code vendor_id}). Mapped to {@code 403 Forbidden} by
 * {@link GlobalExceptionHandler}.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
