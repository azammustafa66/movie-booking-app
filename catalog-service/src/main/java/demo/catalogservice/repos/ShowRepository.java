package demo.catalogservice.repos;

import demo.catalogservice.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {
    /** All showtimes for a movie, e.g. its "showtimes" tab before a city/date is picked. */
    List<Show> findByMovie_Id(Long movieId);

    /** Scheduling check: does this screen already have a show in this time window? */
    List<Show> findByScreen_IdAndStartTimeBetween(Long screenId, LocalDateTime from, LocalDateTime to);

    /** All showtimes at a given theatre on a given day, for a theatre's own listings page. */
    List<Show> findByScreen_Theatre_IdAndStartTimeBetween(Long theatreId, LocalDateTime dayStart, LocalDateTime dayEnd);

    /**
     * Core booking-flow query: showtimes for one movie, in one city, on one day —
     * what the user sees after picking a movie, a city, and a date.
     */
    @Query("""
        SELECT s FROM Show s
        WHERE s.movie.id = :movieId
          AND LOWER(s.screen.theatre.city) = LOWER(:city)
          AND s.startTime BETWEEN :dayStart AND :dayEnd
        ORDER BY s.startTime
    """)
    List<Show> findByMovieAndCityAndDay(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );
}
