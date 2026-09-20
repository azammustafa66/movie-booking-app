package demo.catalogservice.controller.browse;

import demo.catalogservice.dto.response.MovieResponseDto;
import demo.catalogservice.enums.MovieStatus;
import demo.catalogservice.service.browse.MovieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only browse/search endpoints over the movie catalog.
 * <p>
 * Every paginated endpoint here accepts {@code ?sort=<property>,<asc|desc>}
 * (repeatable for a multi-key sort), e.g. {@code ?sort=releaseDate,desc} or
 * {@code ?sort=status&sort=title}. Valid properties are any {@code Movie}
 * field: {@code title}, {@code releaseDate}, {@code status}, {@code
 * durationInMinutes}, {@code certification}, {@code language}. Sorting by
 * {@code genres} isn't supported (it's a collection) and returns {@code 400}.
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MovieController {

    private final MovieService movieService;

    /** All movies in the catalog, regardless of status. */
    @GetMapping
    public ResponseEntity<PagedModel<MovieResponseDto>> getAllMovies(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> movies = movieService.getMovies(pageable);
        return ResponseEntity.ok(new PagedModel<>(movies));
    }

    /** Fetches a single movie by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovieById(
            @PathVariable @Positive(message = "Movie ID cannot be less than 0") Long movieId) {
        MovieResponseDto movie = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movie);
    }

    /** Search-bar lookup as the user types a movie title; matches anywhere in the title, case-insensitively. */
    @GetMapping("/search")
    public ResponseEntity<PagedModel<MovieResponseDto>> getMovieByTitle(
            @NotBlank(message = "Movie title cannot be blank") @RequestParam("title") String title,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> movies = movieService.getMovieByTitle(title, pageable);
        return ResponseEntity.ok(new PagedModel<>(movies));
    }

    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    @GetMapping("/status/{status}")
    public ResponseEntity<PagedModel<MovieResponseDto>> getMoviesByStatus(
            @PathVariable MovieStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> movies = movieService.getMoviesByStatus(status, pageable);
        return ResponseEntity.ok(new PagedModel<>(movies));
    }

    /** Browsing movies filtered by a genre name (e.g. "Action", "Comedy"). */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<PagedModel<MovieResponseDto>> getMoviesByGenre(
            @PathVariable("genre") String genre,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MovieResponseDto> movies = movieService.getMoviesByGenre(genre, pageable);
        return ResponseEntity.ok(new PagedModel<>(movies));
    }
}
