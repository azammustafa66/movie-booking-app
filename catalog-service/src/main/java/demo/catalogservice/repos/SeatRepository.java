package demo.catalogservice.repos;

import demo.catalogservice.entities.Seat;
import demo.catalogservice.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    /** Full seat map for a screen, used to render the seat-selection grid, in row-major reading order. */
    List<Seat> findByScreen_IdOrderByRowLabelAscSeatNumberAsc(Long screenId);

    /** Filtering the seat map by category (e.g. only PREMIUM seats) for pricing/display, in row-major reading order. */
    List<Seat> findByScreen_IdAndSeatTypeOrderByRowLabelAscSeatNumberAsc(Long screenId, SeatType seatType);

    List<Seat> findByScreen_IdAndIdIn(Long screenId, List<Long> seatIds);
}
