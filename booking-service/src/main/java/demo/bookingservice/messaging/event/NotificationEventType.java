package demo.bookingservice.messaging.event;

/** Mirrors notification-service's own {@code NotificationEventType} — the routing key for {@link demo.bookingservice.messaging.BookingEventProducer#publish} is this value's name, lowercased. */
public enum NotificationEventType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED
}