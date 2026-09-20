package demo.notificationservice;

import demo.notificationservice.listener.BookingEventListener;
import demo.notificationservice.messaging.BookingNotificationEvent;
import demo.notificationservice.messaging.NotificationEventType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
    "spring.rabbitmq.listener.simple.auto-startup=false"
})
class NotificationServiceApplicationTests {

    @Autowired
    private BookingEventListener bookingEventListener;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldSendConfirmationEmailOnBookingConfirmedEvent() {
        BookingNotificationEvent event = new BookingNotificationEvent(
                NotificationEventType.BOOKING_CONFIRMED,
                101L,
                202L,
                303L,
                "test@example.com"
        );

        bookingEventListener.handleBookingEvent(event);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("test@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Booking #101 confirmed");
        assertThat(sentMessage.getText()).contains("Your booking #101 for show #303 is confirmed");
        assertThat(sentMessage.getFrom()).isEqualTo("no-reply@moviebooking.local");
    }

    @Test
    void shouldSendCancellationEmailOnBookingCancelledEvent() {
        BookingNotificationEvent event = new BookingNotificationEvent(
                NotificationEventType.BOOKING_CANCELLED,
                102L,
                203L,
                304L,
                "cancel@example.com"
        );

        bookingEventListener.handleBookingEvent(event);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("cancel@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Booking #102 cancelled");
        assertThat(sentMessage.getText()).contains("Your booking #102 for show #304 has been cancelled");
        assertThat(sentMessage.getFrom()).isEqualTo("no-reply@moviebooking.local");
    }
}
