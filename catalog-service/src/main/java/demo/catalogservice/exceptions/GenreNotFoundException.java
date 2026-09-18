package demo.catalogservice.exceptions;

/** Thrown when a genre lookup (by id or name) finds no matching row. */
public class GenreNotFoundException extends ResourceNotFoundException {

    public GenreNotFoundException(String message) {
        super(message);
    }
}
