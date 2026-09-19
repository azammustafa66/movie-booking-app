package demo.catalogservice.exceptions;

/**
 * Thrown when a request body is individually well-formed (passes
 * {@code @Valid}) but violates a domain rule that spans multiple fields,
 * e.g. a show's {@code endTime} not being after its {@code startTime}.
 * Mapped to {@code 400 Bad Request} by {@link GlobalExceptionHandler}.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
