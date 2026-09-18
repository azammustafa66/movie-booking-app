package demo.catalogservice.exceptions;

/** Thrown when a show lookup (by id) finds no matching row. */
public class ShowNotFoundException extends ResourceNotFoundException {

    public ShowNotFoundException(String message) {
        super(message);
    }
}
