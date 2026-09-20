package demo.bookingservice.repos;

import demo.bookingservice.entities.Booking;
import demo.bookingservice.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** A customer's own booking history, e.g. for a "my bookings" screen. */
    List<Booking> findByUserId(Long userId);

    /** Ownership-scoped lookup for any action a customer takes on one of their own bookings (e.g. cancellation). */
    Optional<Booking> findByIdAndUserId(Long bookingId, Long userId);

    /** Every booking against a show, e.g. for an organizer/admin view of who's booked in. */
    List<Booking> findByShowId(Long showId);

    /** Bookings for a show filtered to one status, e.g. only the still-{@code PENDING} ones a sweep might expire. */
    List<Booking> findByShowIdAndStatus(
            Long showId,
            BookingStatus status
    );

    /** Every booking a status sweep should act on — currently {@link demo.bookingservice.service.BookingExpiryService}, for lapsed {@code PENDING} holds. */
    List<Booking> findByStatusAndExpiresAtBefore(
            BookingStatus status,
            LocalDateTime time
    );
}