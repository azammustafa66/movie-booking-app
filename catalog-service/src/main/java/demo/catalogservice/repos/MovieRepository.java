package demo.catalogservice.repos;

import demo.catalogservice.entities.Movie;
import demo.catalogservice.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    List<Movie> findByStatus(MovieStatus status);

    /** Search-bar lookup as the user types a movie title. */
    List<Movie> findByTitleContainingIgnoreCase(String title);

    /** Browsing movies filtered by a genre (e.g. "Action", "Comedy"). */
    List<Movie> findByGenres_NameIgnoreCase(String genreName);
}
