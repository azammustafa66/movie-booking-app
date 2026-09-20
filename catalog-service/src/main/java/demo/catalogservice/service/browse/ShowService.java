package demo.catalogservice.service.browse;

import demo.catalogservice.dto.response.ShowResponseDto;
import demo.catalogservice.entities.Show;
import demo.catalogservice.exceptions.ShowNotFoundException;
import demo.catalogservice.repos.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Read-only showtime lookups backing the "pick a showtime" booking flow. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final ModelMapper modelMapper;

    /**
     * @throws ShowNotFoundException if no show exists with the given id
     */
    public ShowResponseDto getShowById(Long showId) {
        log.info("Fetching show with id {}", showId);
        Show show = showRepository
                .findById(showId)
                .orElseThrow(() -> {
                    log.warn("No show found with id {}", showId);
                    return new ShowNotFoundException("No show found with id " + showId);
                });
        return toDto(show);
    }

    /** All showtimes for a movie, e.g. its "showtimes" tab before a city/date is picked. */
    public Page<ShowResponseDto> getShowsByMovie(Long movieId, Pageable pageable) {
        log.info("Fetching shows for movie with id {}", movieId);
        Page<ShowResponseDto> shows = showRepository.findByMovie_Id(movieId, pageable).map(this::toDto);
        log.info("Found {} shows for movie with id {}", shows.getTotalElements(), movieId);
        return shows;
    }

    /** Scheduling check: does this screen already have a show between {@code from} and {@code to}? */
    public Page<ShowResponseDto> getShowsByScreenAndTimeRange(
            Long screenId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        log.info("Fetching shows for screen with id {} between {} and {}", screenId, from, to);
        Page<ShowResponseDto> shows = showRepository
                .findByScreen_IdAndStartTimeBetween(screenId, from, to, pageable)
                .map(this::toDto);
        log.info("Found {} shows for screen with id {} between {} and {}", shows.getTotalElements(), screenId, from, to);
        return shows;
    }

    /**
     * All showtimes at a given theatre on a given day, for a theatre's own listings
     * page. Not paginated: a single theatre's schedule for a single day is a small,
     * naturally bounded set the frontend needs whole.
     */
    public List<ShowResponseDto> getShowsByTheatreAndDay(Long theatreId, LocalDate day) {
        log.info("Fetching shows for theatre with id {} on {}", theatreId, day);
        List<ShowResponseDto> shows = showRepository
                .findByScreen_Theatre_IdAndStartTimeBetweenOrderByStartTimeAsc(theatreId, startOfDay(day), endOfDay(day))
                .stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} shows for theatre with id {} on {}", shows.size(), theatreId, day);
        return shows;
    }

    /**
     * Core booking-flow query: showtimes for one movie, in one city, on one day —
     * what the user sees after picking a movie, a city, and a date. Not
     * paginated, for the same reason as {@link #getShowsByTheatreAndDay}.
     */
    public List<ShowResponseDto> getShowsByMovieAndCityAndDay(Long movieId, String city, LocalDate day) {
        log.info("Fetching shows for movie with id {} in city '{}' on {}", movieId, city, day);
        List<ShowResponseDto> shows = showRepository
                .findByMovieAndCityAndDay(movieId, city, startOfDay(day), endOfDay(day))
                .stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} shows for movie with id {} in city '{}' on {}", shows.size(), movieId, city, day);
        return shows;
    }

    private LocalDateTime startOfDay(LocalDate day) {
        return day.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate day) {
        return day.atTime(LocalTime.MAX);
    }

    /**
     * Maps the scalar fields and the one-hop {@code movie}/{@code screen}
     * flattening via {@link ModelMapper}, then fills in {@code theatreId}/
     * {@code theatreName} by hand since {@code screen.theatre.*} is two hops
     * away and ModelMapper only flattens one hop automatically.
     */
    private ShowResponseDto toDto(Show show) {
        ShowResponseDto dto = modelMapper.map(show, ShowResponseDto.class);
        dto.setTheatreId(show.getScreen().getTheatre().getId());
        dto.setTheatreName(show.getScreen().getTheatre().getName());
        return dto;
    }
}
