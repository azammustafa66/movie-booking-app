package demo.notificationservice.messaging;

/** Mirrors booking-service's own {@code NotificationEventType} — the two enums must stay in lockstep since values arrive over the wire by name, not by shared code. */
public enum NotificationEventType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED
}