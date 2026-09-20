package demo.bookingservice.messaging;

import demo.bookingservice.messaging.dto.BookingNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes booking lifecycle events to {@code booking.events.exchange} for
 * notification-service to pick up and act on (currently: notify the
 * customer of a confirmation or cancellation). {@code BookingService} calls
 * {@link #publish} while its own database transaction is still open — this
 * isn't a transactional outbox, so the AMQP send and the JPA commit aren't
 * atomic. Since the send happens first, the risk runs one way: a message
 * can go out for a state change whose commit then fails, notifying a
 * customer of something that didn't actually happen. Acceptable for now
 * given how late in each method the call sits (essentially nothing left
 * that could still fail before commit), but a genuine guarantee would need
 * an outbox table instead.
 */
@Component
@RequiredArgsConstructor
public class BookingEventProducer {

    private final RabbitTemplate rabbitTemplate;

    /** Routes on the lowercased event type (e.g. {@code booking_confirmed}) against a {@code #} binding, so any routing key reaches every queue bound to the exchange. */
    public void publish(BookingNotificationEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.BOOKING_EVENTS_EXCHANGE,
                event.type().name().toLowerCase(),
                event
        );
    }
}
