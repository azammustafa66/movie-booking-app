package demo.catalogservice.dto.internal;

import demo.catalogservice.enums.SeatType;

import java.math.BigDecimal;

public record CatalogSeatDto(
        Long seatId,
        String rowLabel,
        Integer seatNumber,
        SeatType seatType,
        BigDecimal price
) {}