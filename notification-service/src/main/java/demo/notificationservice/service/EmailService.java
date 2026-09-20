package demo.notificationservice.service;

import demo.notificationservice.messaging.BookingNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the actual booking confirmation/cancellation email — the real
 * notification action, not a log line standing in for one. Backed by
 * {@code JavaMailSender}, auto-configured by {@code spring-boot-starter-mail}
 * from {@code spring.mail.*} (see {@code application.yaml}); swapping SMTP
 * providers (Gmail, SendGrid, Mailtrap, ...) is a config change here, not a
 * code change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from}")
    private String fromAddress;

    /**
     * @throws org.springframework.mail.MailException if the SMTP server rejects or can't be reached —
     *                                                 left to propagate so the AMQP message is nacked and
     *                                                 redelivered rather than silently dropped
     */
    public void sendBookingNotification(BookingNotificationEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(event.email());
        message.setSubject(subjectFor(event));
        message.setText(bodyFor(event));

        mailSender.send(message);
        log.info("Sent {} email for booking {} to {}", event.type(), event.bookingId(), event.email());
    }

    private String subjectFor(BookingNotificationEvent event) {
        return switch (event.type()) {
            case BOOKING_CONFIRMED -> "Booking #" + event.bookingId() + " confirmed";
            case BOOKING_CANCELLED -> "Booking #" + event.bookingId() + " cancelled";
        };
    }

    private String bodyFor(BookingNotificationEvent event) {
        return switch (event.type()) {
            case BOOKING_CONFIRMED -> """
                    Hi,

                    Your booking #%d for show #%d is confirmed. Enjoy the show!
                    """.formatted(event.bookingId(), event.showId());
            case BOOKING_CANCELLED -> """
                    Hi,

                    Your booking #%d for show #%d has been cancelled. If this wasn't you, please contact support.
                    """.formatted(event.bookingId(), event.showId());
        };
    }
}
