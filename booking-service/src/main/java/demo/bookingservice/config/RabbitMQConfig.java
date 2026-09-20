package demo.bookingservice.config;

import demo.bookingservice.messaging.RabbitMQConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares only the exchange — booking-service is a producer, not a
 * consumer, so it has no reason to know about {@code notification.queue} or
 * how it's bound. notification-service owns that half of the topology (see
 * its own {@code RabbitMQConfig}); RabbitMQ's declare-if-not-exists
 * semantics mean it doesn't matter which service happens to start first.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange bookingEventsExchange() {
        return new TopicExchange(RabbitMQConstants.BOOKING_EVENTS_EXCHANGE);
    }

    /**
     * Without this, Spring AMQP's default {@code SimpleMessageConverter}
     * falls back to Java serialization for a non-{@code String}/{@code byte[]}
     * payload — which fails outright for a {@code record} like
     * {@link demo.bookingservice.messaging.dto.BookingNotificationEvent},
     * since it doesn't implement {@code Serializable}. Spring Boot's
     * autoconfiguration picks up this bean and wires it into the
     * autoconfigured {@code RabbitTemplate} automatically.
     * {@code JacksonJsonMessageConverter}, not the older
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
