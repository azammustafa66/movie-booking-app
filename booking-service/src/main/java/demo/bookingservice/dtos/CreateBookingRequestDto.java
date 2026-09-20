package demo.bookingservice.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Checkout request body. {@code userId} is deliberately not a field — it always comes from the caller's own auth context, never from the request. */
public record CreateBookingRequestDto(
        @NotNull Long showId,
        @NotEmpty List<@NotNull Long> seatIds
) {}