package demo.catalogservice.controller.browse;

import demo.catalogservice.dto.response.ShowResponseDto;
import demo.catalogservice.service.browse.ShowService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only endpoints over showtimes.
 * <p>
 * The paginated endpoints below accept {@code ?sort=<property>,<asc|desc>}
 * (repeatable for a multi-key sort). Valid properties are any {@code Show}
 * field: {@code startTime}, {@code endTime}.
 */
@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ShowController {

    private final ShowService showService;

    /** Fetches a single show by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponseDto> getShowById(
            @PathVariable @Positive(message = "Show ID cannot be less than 0") Long showId) {
        ShowResponseDto show = showService.getShowById(showId);
        return ResponseEntity.ok(show);
    }

    /** All showtimes for a movie, e.g. its "showtimes" tab before a city/date is picked. */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<PagedModel<ShowResponseDto>> getShowsByMovie(
            @PathVariable @Positive(message = "Movie ID cannot be less than 0") Long movieId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ShowResponseDto> shows = showService.getShowsByMovie(movieId, pageable);
        return ResponseEntity.ok(new PagedModel<>(shows));
    }

    /** Scheduling check: does this screen already have a show between {@code from} and {@code to}? */
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<PagedModel<ShowResponseDto>> getShowsByScreenAndTimeRange(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ShowResponseDto> shows = showService.getShowsByScreenAndTimeRange(screenId, from, to, pageable);
        return ResponseEntity.ok(new PagedModel<>(shows));
    }

    /** All showtimes at a given theatre on a given day, for a theatre's own listings page. */
    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<ShowResponseDto>> getShowsByTheatreAndDay(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowResponseDto> shows = showService.getShowsByTheatreAndDay(theatreId, date);
        return ResponseEntity.ok(shows);
    }

    /**
     * Core booking-flow query: showtimes for one movie, in one city, on one day —
     * what the user sees after picking a movie, a city, and a date.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ShowResponseDto>> getShowsByMovieAndCityAndDay(
            @RequestParam @Positive(message = "Movie ID cannot be less than 0") Long movieId,
            @RequestParam @NotBlank(message = "City cannot be blank") String city,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowResponseDto> shows = showService.getShowsByMovieAndCityAndDay(movieId, city, date);
        return ResponseEntity.ok(shows);
    }
}
