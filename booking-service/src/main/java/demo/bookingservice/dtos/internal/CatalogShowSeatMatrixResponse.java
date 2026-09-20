package demo.bookingservice.dtos.internal;

import java.util.List;

/** Response shape for {@code CatalogClient#getSeatMatrix} — a show's static seat map, merged with live status in {@link demo.bookingservice.dtos.SeatMatrixResponse}. */
public record CatalogShowSeatMatrixResponse(
        Long showId,
        Long screenId,
        List<CatalogSeatDto> seats
) {}