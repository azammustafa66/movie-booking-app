package demo.catalogservice.controller;

import demo.catalogservice.dto.MovieResponseDto;
import demo.catalogservice.enums.MovieStatus;
import demo.catalogservice.service.MovieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only browse/search endpoints over the movie catalog. */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MovieController {

    private final MovieService movieService;

    /** All movies in the catalog, regardless of status. */
    @GetMapping
    public ResponseEntity<List<MovieResponseDto>> getAllMovies() {
        List<MovieResponseDto> movies = movieService.getMovies();
        return ResponseEntity.ok(movies);
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
    public ResponseEntity<List<MovieResponseDto>> getMovieByTitle(
            @NotBlank(message = "Movie title cannot be blank") @RequestParam("title") String title) {
        List<MovieResponseDto> movies = movieService.getMovieByTitle(title);
        return ResponseEntity.ok(movies);
    }

    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByStatus(@PathVariable MovieStatus status) {
        List<MovieResponseDto> movies = movieService.getMoviesByStatus(status);
        return ResponseEntity.ok(movies);
    }

    /** Browsing movies filtered by a genre name (e.g. "Action", "Comedy"). */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponseDto>> getMoviesByGenre(@PathVariable("genre") String genre) {
        List<MovieResponseDto> movies = movieService.getMoviesByGenre(genre);
        return ResponseEntity.ok(movies);
    }
}
