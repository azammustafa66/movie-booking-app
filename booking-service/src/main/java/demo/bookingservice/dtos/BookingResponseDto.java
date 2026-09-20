package demo.bookingservice.dtos;

import demo.bookingservice.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Public-facing view of a {@link demo.bookingservice.entities.Booking}. {@code expiresAt} is only meaningful while {@code status} is {@code PENDING} — the hold on its seats lapses at this instant if checkout isn't confirmed first. */
public record BookingResponseDto(
        Long bookingId,
        Long showId,
        BookingStatus status,
        BigDecimal totalAmount,
        LocalDateTime expiresAt
) {}