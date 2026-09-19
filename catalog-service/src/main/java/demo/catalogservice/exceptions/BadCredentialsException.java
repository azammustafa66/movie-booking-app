package demo.catalogservice.exceptions;

import demo.catalogservice.exceptions.GlobalExceptionHandler;

/**
 * Thrown when login credentials, a refresh token, or (in this service) an
 * {@code Authorization} bearer token are missing, malformed, invalid, or
 * expired. Mapped to {@code 401 Unauthorized} by {@link GlobalExceptionHandler}.
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
