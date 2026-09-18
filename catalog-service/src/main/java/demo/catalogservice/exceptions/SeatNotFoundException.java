package demo.catalogservice.exceptions;

/** Thrown when a seat lookup (by id) finds no matching row. */
public class SeatNotFoundException extends ResourceNotFoundException {

    public SeatNotFoundException(String message) {
        super(message);
    }
}
