package demo.catalogservice.exceptions;

/**
 * Base for "no row matched this lookup" exceptions across the catalog
 * (movies, genres, theatres, screens, seats). Mapped to {@code 404 Not Found}
 * by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
