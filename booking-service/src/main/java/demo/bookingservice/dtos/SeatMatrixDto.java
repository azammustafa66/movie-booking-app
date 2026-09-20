package demo.bookingservice.dtos;

import demo.bookingservice.enums.SeatType;
import demo.bookingservice.enums.ShowSeatStatus;

import java.math.BigDecimal;

/**
 * One seat in a {@link SeatMatrixResponse}: catalog-service's static shape
 * ({@code rowLabel}/{@code seatNumber}/{@code seatType}/{@code price})
 * merged with booking-service's own live {@code status}.
 */
public record SeatMatrixDto(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        BigDecimal price,
        ShowSeatStatus status
) {}