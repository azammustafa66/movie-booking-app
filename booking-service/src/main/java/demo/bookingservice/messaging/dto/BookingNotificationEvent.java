package demo.bookingservice.messaging.dto;

import demo.bookingservice.messaging.event.NotificationEventType;

/**
 * The AMQP message body published by {@link demo.bookingservice.messaging.BookingEventProducer}.
 * Mirrors notification-service's own {@code BookingNotificationEvent} by
 * JSON shape ({@code demo.bookingservice.config.RabbitMQConfig}'s
 * {@code JacksonJsonMessageConverter}), not code — the two
 * services don't share a library for this. {@code email} rides along so
 * notification-service can actually send mail without needing its own copy
 * of {@code app_users} — it has no database of its own.
 */
public record BookingNotificationEvent(
        NotificationEventType type,
        Long bookingId,
        Long userId,
        Long showId,
        String email
) {}