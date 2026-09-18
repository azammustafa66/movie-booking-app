package demo.catalogservice.repos;

import demo.catalogservice.entities.Movie;
import demo.catalogservice.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    /** Search-bar lookup as the user types a movie title. */
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /** Browsing movies filtered by a genre (e.g. "Action", "Comedy"). */
    Page<Movie> findByGenres_NameIgnoreCase(String genreName, Pageable pageable);
}
