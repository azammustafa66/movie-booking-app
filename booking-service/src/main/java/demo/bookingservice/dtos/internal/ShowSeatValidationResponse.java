package demo.bookingservice.dtos.internal;

import java.util.List;

/** Response shape for {@code CatalogClient#validateSeats} — confirms every requested seat id is real and returns its price. */
public record ShowSeatValidationResponse(
        Long showId,
        Long screenId,
        List<ValidatedSeatDto> seats
) {}