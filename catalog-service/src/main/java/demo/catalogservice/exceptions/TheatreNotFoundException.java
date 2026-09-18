package demo.catalogservice.exceptions;

/** Thrown when a theatre lookup (by id) finds no matching row. */
public class TheatreNotFoundException extends ResourceNotFoundException {

    public TheatreNotFoundException(String message) {
        super(message);
    }
}
