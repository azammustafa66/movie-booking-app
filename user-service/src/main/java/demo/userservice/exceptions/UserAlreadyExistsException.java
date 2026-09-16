package demo.userservice.exceptions;

/**
 * Thrown on signup when the given email is already registered. Mapped to
 * {@code 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
