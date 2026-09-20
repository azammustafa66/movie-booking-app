package demo.notificationservice.messaging;

/**
 * The AMQP message body consumed by {@link demo.notificationservice.listener.BookingEventListener}.
 * Mirrors booking-service's own {@code BookingNotificationEvent} by JSON
 * shape ({@link demo.notificationservice.config.RabbitMQConfig}'s
 * {@code JacksonJsonMessageConverter}), not code — the two services don't
 * share a library for this. {@code email} is who
 * {@link demo.notificationservice.service.EmailService} actually sends to —
 * this service has no database of its own, so it has no other way to
 * resolve a user id to an address.
 */
public record BookingNotificationEvent(
        NotificationEventType type,
        Long bookingId,
        Long userId,
        Long showId,
        String email
) {}