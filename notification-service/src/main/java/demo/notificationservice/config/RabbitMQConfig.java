package demo.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the full topology this service depends on: the exchange
 * booking-service publishes to (redeclared here too, since RabbitMQ's
 * declare-if-not-exists semantics mean it doesn't matter which service
 * starts first), this service's own queue, and a catch-all ({@code #})
 * binding between them — every booking event reaches this one queue
 * regardless of routing key, since there's only one consumer today.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange bookingEventsExchange() {
        return new TopicExchange("booking.events.exchange");
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue("notification.queue");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange bookingEventsExchange) {

        return BindingBuilder
                .bind(notificationQueue)
                .to(bookingEventsExchange)
                .with("#");
    }

    /**
     * Without this, Spring AMQP's default {@code SimpleMessageConverter}
     * falls back to Java deserialization for an incoming message body,
     * which can't reconstruct a {@code record} like
     * {@link demo.notificationservice.messaging.BookingNotificationEvent}
     * (it isn't {@code Serializable}) and wouldn't match booking-service's
     * JSON payload anyway. Spring Boot's autoconfiguration picks up this
     * bean and wires it into the {@code @RabbitListener} container
     * automatically. {@code JacksonJsonMessageConverter}, not the older
     * {@code Jackson2JsonMessageConverter} — Spring Boot 4 pulls in Jackson 3
     * ({@code tools.jackson.*}) by default, and {@code Jackson2JsonMessageConverter}
     * hard-requires the legacy Jackson 2 ({@code com.fasterxml.jackson.*})
     * classes, which aren't on the classpath here and throw
     * {@code NoClassDefFoundError} at startup if used.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}