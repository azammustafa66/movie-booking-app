package demo.catalogservice.exceptions;

/** Thrown when a screen lookup (by id) finds no matching row. */
public class ScreenNotFoundException extends ResourceNotFoundException {

    public ScreenNotFoundException(String message) {
        super(message);
    }
}
