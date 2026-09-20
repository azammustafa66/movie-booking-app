package demo.bookingservice.repos;

import demo.bookingservice.entities.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /** The seats on one booking, e.g. for a booking-detail/receipt view. */
    List<BookingSeat> findByBookingId(Long bookingId);

    /** Every booking that has ever claimed a given seat id, across shows. */
    List<BookingSeat> findBySeatId(Long seatId);
}