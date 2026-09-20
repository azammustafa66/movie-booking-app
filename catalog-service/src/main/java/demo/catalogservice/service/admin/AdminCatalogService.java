package demo.catalogservice.service.admin;

import demo.catalogservice.dto.response.GenreResponseDto;
import demo.catalogservice.dto.response.MovieResponseDto;
import demo.catalogservice.dto.response.ScreenResponseDto;
import demo.catalogservice.dto.response.SeatResponseDto;
import demo.catalogservice.dto.response.ShowResponseDto;
import demo.catalogservice.dto.response.TheatreResponseDto;
import demo.catalogservice.dto.request.AdminTheatreRequestDto;
import demo.catalogservice.dto.request.GenreRequestDto;
import demo.catalogservice.dto.request.MovieRequestDto;
import demo.catalogservice.dto.request.ScreenRequestDto;
import demo.catalogservice.dto.request.SeatRequestDto;
import demo.catalogservice.dto.request.ShowRequestDto;
import demo.catalogservice.entities.Genre;
import demo.catalogservice.entities.Movie;
import demo.catalogservice.entities.Screen;
import demo.catalogservice.entities.Seat;
import demo.catalogservice.entities.Show;
import demo.catalogservice.entities.Theatre;
import demo.catalogservice.exceptions.GenreNotFoundException;
import demo.catalogservice.exceptions.InvalidRequestException;
import demo.catalogservice.exceptions.MovieNotFoundException;
import demo.catalogservice.exceptions.ScreenNotFoundException;
import demo.catalogservice.exceptions.SeatNotFoundException;
import demo.catalogservice.exceptions.ShowNotFoundException;
import demo.catalogservice.exceptions.TheatreNotFoundException;
import demo.catalogservice.repos.GenreRepository;
import demo.catalogservice.repos.MovieRepository;
import demo.catalogservice.repos.ScreenRepository;
import demo.catalogservice.repos.SeatRepository;
import demo.catalogservice.repos.ShowRepository;
import demo.catalogservice.repos.TheatreRepository;
import demo.catalogservice.service.vendor.VendorCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Write-side of the catalog for platform admins.
 * <p>
 * Unlike {@link VendorCatalogService}, nothing here is ownership-scoped —
 * an admin can create, update, or delete any theatre, screen, seat, show,
 * movie, or genre. Authorization (is the caller actually an admin?) is
 * enforced upstream by {@code AuthInterceptor} on every
 * {@code /api/v1/admin/**} route, so this service trusts every call it
 * receives.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCatalogService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    // ------------------------------------------------------------------
    // Theatres
    // ------------------------------------------------------------------

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public TheatreResponseDto createTheatre(AdminTheatreRequestDto request) {
        log.info("Admin creating theatre '{}' for vendor {}", request.getName(), request.getVendorId());
        Theatre theatre = new Theatre();
        applyTheatreFields(theatre, request);
        theatre.setVendorId(request.getVendorId());
        theatre = theatreRepository.save(theatre);
        return toDto(theatre);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public TheatreResponseDto updateTheatre(Long theatreId, AdminTheatreRequestDto request) {
        log.info("Admin updating theatre with id {}", theatreId);
        Theatre theatre = findTheatreOrThrow(theatreId);
        applyTheatreFields(theatre, request);
        theatre.setVendorId(request.getVendorId());
        theatre = theatreRepository.save(theatre);
        return toDto(theatre);
    }

    /**
     * @throws TheatreNotFoundException     if no theatre exists with the given id
     * @throws org.springframework.dao.DataIntegrityViolationException if the theatre still has
     *                                       screens referencing it — delete those first
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public void deleteTheatre(Long theatreId) {
        log.info("Admin deleting theatre with id {}", theatreId);
        Theatre theatre = findTheatreOrThrow(theatreId);
        theatreRepository.delete(theatre);
    }

    private void applyTheatreFields(Theatre theatre, AdminTheatreRequestDto request) {
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setCity(request.getCity());
        theatre.setState(request.getState());
        theatre.setPincode(request.getPincode());
    }

    // ------------------------------------------------------------------
    // Screens
    // ------------------------------------------------------------------

    @Transactional
    public ScreenResponseDto createScreen(ScreenRequestDto request) {
        log.info("Admin creating screen '{}' for theatre {}", request.getName(), request.getTheatreId());
        Screen screen = new Screen();
        screen.setTheatre(findTheatreOrThrow(request.getTheatreId()));
        applyScreenFields(screen, request);
        screen = screenRepository.save(screen);
        return toDto(screen);
    }

    @Transactional
    public ScreenResponseDto updateScreen(Long screenId, ScreenRequestDto request) {
        log.info("Admin updating screen with id {}", screenId);
        Screen screen = findScreenOrThrow(screenId);
        if (!screen.getTheatre().getId().equals(request.getTheatreId())) {
            screen.setTheatre(findTheatreOrThrow(request.getTheatreId()));
        }
        applyScreenFields(screen, request);
        screen = screenRepository.save(screen);
        return toDto(screen);
    }

    /**
     * @throws ScreenNotFoundException      if no screen exists with the given id
     * @throws org.springframework.dao.DataIntegrityViolationException if the screen still has
     *                                       seats or shows referencing it — delete those first
     */
    @Transactional
    public void deleteScreen(Long screenId) {
        log.info("Admin deleting screen with id {}", screenId);
        Screen screen = findScreenOrThrow(screenId);
        screenRepository.delete(screen);
    }

    private void applyScreenFields(Screen screen, ScreenRequestDto request) {
        screen.setName(request.getName());
        screen.setCapacity(request.getCapacity());
        screen.setScreenType(request.getScreenType());
    }

    // ------------------------------------------------------------------
    // Seats
    // ------------------------------------------------------------------

    @Transactional
    public SeatResponseDto createSeat(SeatRequestDto request) {
        log.info("Admin creating seat {}{} for screen {}", request.getRowLabel(), request.getSeatNumber(), request.getScreenId());
        Seat seat = new Seat();
        seat.setScreen(findScreenOrThrow(request.getScreenId()));
        applySeatFields(seat, request);
        seat = seatRepository.save(seat);
        return toDto(seat);
    }

    @Transactional
    public SeatResponseDto updateSeat(Long seatId, SeatRequestDto request) {
        log.info("Admin updating seat with id {}", seatId);
        Seat seat = findSeatOrThrow(seatId);
        if (!seat.getScreen().getId().equals(request.getScreenId())) {
            seat.setScreen(findScreenOrThrow(request.getScreenId()));
        }
        applySeatFields(seat, request);
        seat = seatRepository.save(seat);
        return toDto(seat);
    }

    /**
     * @throws SeatNotFoundException if no seat exists with the given id
     */
    @Transactional
    public void deleteSeat(Long seatId) {
        log.info("Admin deleting seat with id {}", seatId);
        Seat seat = findSeatOrThrow(seatId);
        seatRepository.delete(seat);
    }

    private void applySeatFields(Seat seat, SeatRequestDto request) {
        seat.setRowLabel(request.getRowLabel());
        seat.setSeatNumber(request.getSeatNumber());
        seat.setSeatType(request.getSeatType());
        seat.setPrice(request.getPrice());
    }

    // ------------------------------------------------------------------
    // Shows
    // ------------------------------------------------------------------

    @Transactional
    public ShowResponseDto createShow(ShowRequestDto request) {
        log.info("Admin creating show for movie {} on screen {}", request.getMovieId(), request.getScreenId());
        Show show = new Show();
        show.setMovie(findMovieOrThrow(request.getMovieId()));
        show.setScreen(findScreenOrThrow(request.getScreenId()));
        applyShowFields(show, request);
        show = showRepository.save(show);
        return toDto(show);
    }

    @Transactional
    public ShowResponseDto updateShow(Long showId, ShowRequestDto request) {
        log.info("Admin updating show with id {}", showId);
        Show show = findShowOrThrow(showId);
        if (!show.getMovie().getId().equals(request.getMovieId())) {
            show.setMovie(findMovieOrThrow(request.getMovieId()));
        }
        if (!show.getScreen().getId().equals(request.getScreenId())) {
            show.setScreen(findScreenOrThrow(request.getScreenId()));
        }
        applyShowFields(show, request);
        show = showRepository.save(show);
        return toDto(show);
    }

    /**
     * @throws ShowNotFoundException if no show exists with the given id
     */
    @Transactional
    public void deleteShow(Long showId) {
        log.info("Admin deleting show with id {}", showId);
        Show show = findShowOrThrow(showId);
        showRepository.delete(show);
    }

    private void applyShowFields(Show show, ShowRequestDto request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidRequestException("Show end time must be after its start time");
        }
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
    }

    // ------------------------------------------------------------------
    // Movies
    // ------------------------------------------------------------------

    @Transactional
    @CacheEvict(value = "movieSearch", allEntries = true)
    public MovieResponseDto createMovie(MovieRequestDto request) {
        log.info("Admin creating movie '{}'", request.getTitle());
        Movie movie = new Movie();
        applyMovieFields(movie, request);
        movie = movieRepository.save(movie);
        return toDto(movie);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", key = "#movieId"),
            @CacheEvict(value = "movieSearch", allEntries = true)
    })
    public MovieResponseDto updateMovie(Long movieId, MovieRequestDto request) {
        log.info("Admin updating movie with id {}", movieId);
        Movie movie = findMovieOrThrow(movieId);
        applyMovieFields(movie, request);
        movie = movieRepository.save(movie);
        return toDto(movie);
    }

    /**
     * Deletes a movie, first clearing its genre tags so the {@code movie_genres}
     * join rows (owned by this side of the many-to-many) don't block the delete.
     * Shows already scheduled for this movie are <em>not</em> touched — if any
     * exist, the delete fails with a {@code 409} instead of silently orphaning them.
     *
     * @throws MovieNotFoundException if no movie exists with the given id
     * @throws org.springframework.dao.DataIntegrityViolationException if the movie still has
     *                                       shows referencing it — delete those first
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", key = "#movieId"),
            @CacheEvict(value = "movieSearch", allEntries = true)
    })
    public void deleteMovie(Long movieId) {
        log.info("Admin deleting movie with id {}", movieId);
        Movie movie = findMovieOrThrow(movieId);
        movie.getGenres().clear();
        movieRepository.save(movie);
        movieRepository.delete(movie);
    }

    private void applyMovieFields(Movie movie, MovieRequestDto request) {
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setLanguage(request.getLanguage());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setCertification(request.getCertification());
        movie.setDurationInMinutes(request.getDurationInMinutes());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setStatus(request.getStatus());
        movie.setGenres(resolveGenres(request.getGenreIds()));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
        if (genres.size() != genreIds.size()) {
            Set<Long> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(genreIds);
            missingIds.removeAll(foundIds);
            throw new GenreNotFoundException("No genre(s) found with id(s) " + missingIds);
        }
        return genres;
    }

    // ------------------------------------------------------------------
    // Genres
    // ------------------------------------------------------------------

    @Transactional
    public GenreResponseDto createGenre(GenreRequestDto request) {
        log.info("Admin creating genre '{}'", request.getName());
        Genre genre = new Genre();
        genre.setName(request.getName());
        genre = genreRepository.save(genre);
        return toDto(genre);
    }

    @Transactional
    public GenreResponseDto updateGenre(Long genreId, GenreRequestDto request) {
        log.info("Admin updating genre with id {}", genreId);
        Genre genre = findGenreOrThrow(genreId);
        genre.setName(request.getName());
        genre = genreRepository.save(genre);
        return toDto(genre);
    }

    /**
     * Deletes a genre, first untagging it from every movie that references it
     * (this is the {@code mappedBy} side of the many-to-many, so those
     * {@code movie_genres} rows have to be removed via the owning {@code Movie}
     * side rather than by touching this genre alone).
     *
     * @throws GenreNotFoundException if no genre exists with the given id
     */
    @Transactional
    public void deleteGenre(Long genreId) {
        log.info("Admin deleting genre with id {}", genreId);
        Genre genre = findGenreOrThrow(genreId);
        for (Movie movie : new HashSet<>(genre.getMovies())) {
            movie.getGenres().remove(genre);
            movieRepository.save(movie);
        }
        genreRepository.delete(genre);
    }

    // ------------------------------------------------------------------
    // Lookups shared across the entity groups above
    // ------------------------------------------------------------------

    private Theatre findTheatreOrThrow(Long theatreId) {
        return theatreRepository.findById(theatreId)
                .orElseThrow(() -> new TheatreNotFoundException("No theatre found with id " + theatreId));
    }

    private Screen findScreenOrThrow(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("No screen found with id " + screenId));
    }

    private Seat findSeatOrThrow(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException("No seat found with id " + seatId));
    }

    private Show findShowOrThrow(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("No show found with id " + showId));
    }

    private Movie findMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("No movie found with id " + movieId));
    }

    private Genre findGenreOrThrow(Long genreId) {
        return genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("No genre found with id " + genreId));
    }

    // ------------------------------------------------------------------
    // Entity -> response DTO mapping
    // ------------------------------------------------------------------

    private TheatreResponseDto toDto(Theatre theatre) {
        return modelMapper.map(theatre, TheatreResponseDto.class);
    }

    private ScreenResponseDto toDto(Screen screen) {
        return modelMapper.map(screen, ScreenResponseDto.class);
    }

    private SeatResponseDto toDto(Seat seat) {
        return modelMapper.map(seat, SeatResponseDto.class);
    }

    private ShowResponseDto toDto(Show show) {
        ShowResponseDto dto = modelMapper.map(show, ShowResponseDto.class);
        dto.setTheatreId(show.getScreen().getTheatre().getId());
        dto.setTheatreName(show.getScreen().getTheatre().getName());
        return dto;
    }

    private GenreResponseDto toDto(Genre genre) {
        return modelMapper.map(genre, GenreResponseDto.class);
    }

    private MovieResponseDto toDto(Movie movie) {
        MovieResponseDto dto = modelMapper.map(movie, MovieResponseDto.class);
        dto.setGenres(movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toCollection(TreeSet::new)));
        return dto;
    }
}
