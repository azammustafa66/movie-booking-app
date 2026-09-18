package demo.catalogservice.exceptions;

/** Thrown when a movie lookup (by id) finds no matching row. */
public class MovieNotFoundException extends ResourceNotFoundException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}
