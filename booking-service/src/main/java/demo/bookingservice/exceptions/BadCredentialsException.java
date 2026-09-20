package demo.bookingservice.exceptions;

/**
 * Thrown when the {@code X-User-Id}/{@code X-User-Role}/{@code X-User-Email}
 * headers the API gateway is supposed to set are missing or malformed — see
 * {@code AuthInterceptor}. Mapped to {@code 401 Unauthorized} by
 * {@link GlobalExceptionHandler}.
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
