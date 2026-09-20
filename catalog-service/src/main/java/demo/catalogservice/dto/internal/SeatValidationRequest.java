package demo.catalogservice.dto.internal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** The seat ids booking-service wants to hold, sent to {@code POST /internal/catalog/shows/{showId}/validate-seats}. */
public record SeatValidationRequest(
        @NotEmpty(message = "Seat IDs cannot be empty")
        List<@NotNull Long> seatIds
) {
}