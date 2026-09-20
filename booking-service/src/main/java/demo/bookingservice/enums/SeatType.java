package demo.bookingservice.enums;

/**
 * Mirrors {@code demo.catalogservice.enums.SeatType} in catalog-service —
 * used only to deserialize the {@code seatType} field catalog-service sends
 * back in {@link demo.bookingservice.dtos.internal.CatalogSeatDto}.
 */
public enum SeatType {
    REGULAR,
    PREMIUM,
    RECLINER
}