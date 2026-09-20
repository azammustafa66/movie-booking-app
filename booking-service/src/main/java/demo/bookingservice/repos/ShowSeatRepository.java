package demo.bookingservice.repos;

import demo.bookingservice.entities.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    /**
     * The one row tracking a seat's lock/booking status for a show, row-locked
     * ({@code SELECT ... FOR UPDATE}) so two concurrent requests for the same
     * seat serialize instead of both seeing it as available. Empty when no
     * one has ever locked or booked this seat for this show — see
     * {@code BookingService#lockSeat}, which creates the row on first touch
     * rather than treating that as an error.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ShowSeat> findByShowIdAndSeatId(Long showId, Long seatId);

    /** Every seat with a status row for a show, for merging into catalog-service's static seat map. */
    List<ShowSeat> findByShowId(Long showId);
}