package demo.catalogservice.exceptions;

import demo.catalogservice.exceptions.GlobalExceptionHandler;

/**
 * Thrown when login credentials or a refresh token are invalid, unknown,
 * revoked, or expired. Mapped to {@code 401 Unauthorized} by
 * {@link GlobalExceptionHandler}.
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
