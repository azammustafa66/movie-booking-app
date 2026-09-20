package demo.bookingservice.messaging;

/**
 * Exchange/queue names shared with notification-service — kept as literal
 * strings on both sides (each service declares its own topology) rather
 * than a shared library, so {@code NOTIFICATION_QUEUE} here is purely
 * documentation of what the other side is named; nothing in this service
 * actually binds to it.
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    public static final String BOOKING_EVENTS_EXCHANGE = "booking.events.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
}
