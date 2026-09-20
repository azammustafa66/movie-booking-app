package demo.bookingservice.enums;

/**
 * Mirrors {@code demo.userservice.enums.Role} in user-service. Booking-service
 * never creates users; it only reads this value out of the {@code role} claim
 * of a JWT that user-service already issued.
 */
public enum Role {
    CUSTOMER,
    ADMIN,
    VENDOR
}
