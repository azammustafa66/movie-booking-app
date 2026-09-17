package demo.catalogservice.repos;

import demo.catalogservice.entities.Seat;
import demo.catalogservice.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /** Full seat map for a screen, used to render the seat-selection grid. */
    List<Seat> findByScreen_Id(Long screenId);

    /** Filtering the seat map by category (e.g. only PREMIUM seats) for pricing/display. */
    List<Seat> findByScreen_IdAndSeatType(Long screenId, SeatType seatType);
}
