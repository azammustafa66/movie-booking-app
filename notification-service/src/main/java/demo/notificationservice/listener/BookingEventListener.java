package demo.notificationservice.listener;

import demo.notificationservice.messaging.BookingNotificationEvent;
import demo.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes every message on {@code notification.queue} — one listener for
 * both {@link demo.notificationservice.messaging.NotificationEventType}
 * values, since there's currently only one thing to do with either: email
 * the customer via {@link EmailService}. A message that throws here is
 * nacked and, by Spring AMQP's default retry policy, redelivered a few
 * times before landing nowhere (no dead-letter queue is configured), so a
 * malformed event or a downstream mail failure can be silently dropped
 * after retries are exhausted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = "notification.queue")
    public void handleBookingEvent(BookingNotificationEvent event) {
        emailService.sendBookingNotification(event);
    }
}
