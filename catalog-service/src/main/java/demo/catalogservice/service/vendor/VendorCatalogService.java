package demo.catalogservice.service.vendor;

import demo.catalogservice.dto.response.ScreenResponseDto;
import demo.catalogservice.dto.response.SeatResponseDto;
import demo.catalogservice.dto.response.ShowResponseDto;
import demo.catalogservice.dto.response.TheatreResponseDto;
import demo.catalogservice.dto.request.ScreenRequestDto;
import demo.catalogservice.dto.request.SeatRequestDto;
import demo.catalogservice.dto.request.ShowRequestDto;
import demo.catalogservice.dto.request.VendorTheatreRequestDto;
import demo.catalogservice.entities.Movie;
import demo.catalogservice.entities.Screen;
import demo.catalogservice.entities.Seat;
import demo.catalogservice.entities.Show;
import demo.catalogservice.entities.Theatre;
import demo.catalogservice.exceptions.ForbiddenException;
import demo.catalogservice.exceptions.InvalidRequestException;
import demo.catalogservice.exceptions.MovieNotFoundException;
import demo.catalogservice.exceptions.ScreenNotFoundException;
import demo.catalogservice.exceptions.SeatNotFoundException;
import demo.catalogservice.exceptions.ShowNotFoundException;
import demo.catalogservice.exceptions.TheatreNotFoundException;
import demo.catalogservice.repos.MovieRepository;
import demo.catalogservice.repos.ScreenRepository;
import demo.catalogservice.repos.SeatRepository;
import demo.catalogservice.repos.ShowRepository;
import demo.catalogservice.repos.TheatreRepository;
import demo.catalogservice.service.admin.AdminCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-side of the catalog for vendors, scoped to what they own.
 * <p>
 * Every method here takes the caller's {@code vendorId} (resolved by
 * {@code AuthInterceptor} from the gateway-forwarded
 * {@code X-User-Id} header) and uses it two ways:
 * <ul>
 *     <li>on create, to stamp ownership onto the new theatre (or, for
 *     screens/seats/shows, to require the parent theatre already belongs
 *     to this vendor);</li>
 *     <li>on read/update/delete, to verify the target resource's theatre
 *     belongs to this vendor before touching it, throwing
 *     {@link ForbiddenException} (403) otherwise.</li>
 * </ul>
 * A vendor can never see or affect another vendor's theatres, screens,
 * seats, or shows. Movies and genres aren't vendor-owned, so they're not
 * exposed here at all — see {@link AdminCatalogService} for those.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorCatalogService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    // ------------------------------------------------------------------
    // Theatres
    // ------------------------------------------------------------------

    /** All theatres owned by this vendor, for their "my theatres" management screen. */
    public Page<TheatreResponseDto> getMyTheatres(Long vendorId, Pageable pageable) {
        log.info("Fetching theatres owned by vendor {}", vendorId);
        return theatreRepository.findByVendorId(vendorId, pageable).map(this::toDto);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public TheatreResponseDto createTheatre(Long vendorId, VendorTheatreRequestDto request) {
        log.info("Vendor {} creating theatre '{}'", vendorId, request.getName());
        Theatre theatre = new Theatre();
        applyTheatreFields(theatre, request);
        theatre.setVendorId(vendorId);
        theatre = theatreRepository.save(theatre);
        return toDto(theatre);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public TheatreResponseDto updateTheatre(Long vendorId, Long theatreId, VendorTheatreRequestDto request) {
        log.info("Vendor {} updating theatre with id {}", vendorId, theatreId);
        Theatre theatre = requireOwnedTheatre(vendorId, theatreId);
        applyTheatreFields(theatre, request);
        theatre = theatreRepository.save(theatre);
        return toDto(theatre);
    }

    /**
     * @throws TheatreNotFoundException     if no theatre exists with the given id
     * @throws ForbiddenException           if the theatre belongs to a different vendor
     * @throws org.springframework.dao.DataIntegrityViolationException if the theatre still has
     *                                       screens referencing it — delete those first
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "theatresByCity", allEntries = true),
            @CacheEvict(value = "theatreByName", allEntries = true)
    })
    public void deleteTheatre(Long vendorId, Long theatreId) {
        log.info("Vendor {} deleting theatre with id {}", vendorId, theatreId);
        Theatre theatre = requireOwnedTheatre(vendorId, theatreId);
        theatreRepository.delete(theatre);
    }

    private void applyTheatreFields(Theatre theatre, VendorTheatreRequestDto request) {
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
    public ScreenResponseDto createScreen(Long vendorId, ScreenRequestDto request) {
        log.info("Vendor {} creating screen '{}' for theatre {}", vendorId, request.getName(), request.getTheatreId());
        Screen screen = new Screen();
        screen.setTheatre(requireOwnedTheatre(vendorId, request.getTheatreId()));
        applyScreenFields(screen, request);
        screen = screenRepository.save(screen);
        return toDto(screen);
    }

    @Transactional
    public ScreenResponseDto updateScreen(Long vendorId, Long screenId, ScreenRequestDto request) {
        log.info("Vendor {} updating screen with id {}", vendorId, screenId);
        Screen screen = requireOwnedScreen(vendorId, screenId);
        if (!screen.getTheatre().getId().equals(request.getTheatreId())) {
            // Re-parenting to a different theatre must also be one of this vendor's own.
            screen.setTheatre(requireOwnedTheatre(vendorId, request.getTheatreId()));
        }
        applyScreenFields(screen, request);
        screen = screenRepository.save(screen);
        return toDto(screen);
    }

    /**
     * @throws ScreenNotFoundException      if no screen exists with the given id
     * @throws ForbiddenException           if the screen's theatre belongs to a different vendor
     * @throws org.springframework.dao.DataIntegrityViolationException if the screen still has
     *                                       seats or shows referencing it — delete those first
     */
    @Transactional
    public void deleteScreen(Long vendorId, Long screenId) {
        log.info("Vendor {} deleting screen with id {}", vendorId, screenId);
        Screen screen = requireOwnedScreen(vendorId, screenId);
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
    public SeatResponseDto createSeat(Long vendorId, SeatRequestDto request) {
        log.info("Vendor {} creating seat {}{} for screen {}", vendorId, request.getRowLabel(), request.getSeatNumber(), request.getScreenId());
        Seat seat = new Seat();
        seat.setScreen(requireOwnedScreen(vendorId, request.getScreenId()));
        applySeatFields(seat, request);
        seat = seatRepository.save(seat);
        return toDto(seat);
    }

    @Transactional
    public SeatResponseDto updateSeat(Long vendorId, Long seatId, SeatRequestDto request) {
        log.info("Vendor {} updating seat with id {}", vendorId, seatId);
        Seat seat = requireOwnedSeat(vendorId, seatId);
        if (!seat.getScreen().getId().equals(request.getScreenId())) {
            seat.setScreen(requireOwnedScreen(vendorId, request.getScreenId()));
        }
        applySeatFields(seat, request);
        seat = seatRepository.save(seat);
        return toDto(seat);
    }

    /**
     * @throws SeatNotFoundException if no seat exists with the given id
     * @throws ForbiddenException    if the seat's theatre belongs to a different vendor
     */
    @Transactional
    public void deleteSeat(Long vendorId, Long seatId) {
        log.info("Vendor {} deleting seat with id {}", vendorId, seatId);
        Seat seat = requireOwnedSeat(vendorId, seatId);
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

    /** Any existing catalog movie may be scheduled — movies aren't vendor-owned. */
    @Transactional
    public ShowResponseDto createShow(Long vendorId, ShowRequestDto request) {
        log.info("Vendor {} creating show for movie {} on screen {}", vendorId, request.getMovieId(), request.getScreenId());
        Show show = new Show();
        show.setMovie(findMovieOrThrow(request.getMovieId()));
        show.setScreen(requireOwnedScreen(vendorId, request.getScreenId()));
        applyShowFields(show, request);
        show = showRepository.save(show);
        return toDto(show);
    }

    @Transactional
    public ShowResponseDto updateShow(Long vendorId, Long showId, ShowRequestDto request) {
        log.info("Vendor {} updating show with id {}", vendorId, showId);
        Show show = requireOwnedShow(vendorId, showId);
        if (!show.getMovie().getId().equals(request.getMovieId())) {
            show.setMovie(findMovieOrThrow(request.getMovieId()));
        }
        if (!show.getScreen().getId().equals(request.getScreenId())) {
            show.setScreen(requireOwnedScreen(vendorId, request.getScreenId()));
        }
        applyShowFields(show, request);
        show = showRepository.save(show);
        return toDto(show);
    }

    /**
     * @throws ShowNotFoundException if no show exists with the given id
     * @throws ForbiddenException    if the show's theatre belongs to a different vendor
     */
    @Transactional
    public void deleteShow(Long vendorId, Long showId) {
        log.info("Vendor {} deleting show with id {}", vendorId, showId);
        Show show = requireOwnedShow(vendorId, showId);
        showRepository.delete(show);
    }

    private void applyShowFields(Show show, ShowRequestDto request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidRequestException("Show end time must be after its start time");
        }
        show.setStartTime(request.getStartTime());
        show.setEndTime(request.getEndTime());
    }

    private Movie findMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException("No movie found with id " + movieId));
    }

    // ------------------------------------------------------------------
    // Ownership checks
    // ------------------------------------------------------------------

    /**
     * @throws TheatreNotFoundException if no theatre exists with the given id
     * @throws ForbiddenException       if it exists but isn't owned by {@code vendorId}
     */
    private Theatre requireOwnedTheatre(Long vendorId, Long theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new TheatreNotFoundException("No theatre found with id " + theatreId));
        if (!theatre.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Theatre " + theatreId + " does not belong to this vendor");
        }
        return theatre;
    }

    /**
     * @throws ScreenNotFoundException if no screen exists with the given id
     * @throws ForbiddenException      if its theatre isn't owned by {@code vendorId}
     */
    private Screen requireOwnedScreen(Long vendorId, Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("No screen found with id " + screenId));
        if (!screen.getTheatre().getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Screen " + screenId + " does not belong to this vendor");
        }
        return screen;
    }

    /**
     * @throws SeatNotFoundException if no seat exists with the given id
     * @throws ForbiddenException    if its theatre isn't owned by {@code vendorId}
     */
    private Seat requireOwnedSeat(Long vendorId, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException("No seat found with id " + seatId));
        if (!seat.getScreen().getTheatre().getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Seat " + seatId + " does not belong to this vendor");
        }
        return seat;
    }

    /**
     * @throws ShowNotFoundException if no show exists with the given id
     * @throws ForbiddenException    if its theatre isn't owned by {@code vendorId}
     */
    private Show requireOwnedShow(Long vendorId, Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("No show found with id " + showId));
        if (!show.getScreen().getTheatre().getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Show " + showId + " does not belong to this vendor");
        }
        return show;
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
}
