package demo.catalogservice.service;

import demo.catalogservice.dto.SeatResponseDto;
import demo.catalogservice.entities.Seat;
import demo.catalogservice.enums.SeatType;
import demo.catalogservice.exceptions.SeatNotFoundException;
import demo.catalogservice.repos.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/** Read-only seat lookups backing the seat-selection grid. */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;

    /** All seats across every screen. */
    public Page<SeatResponseDto> getSeats(Pageable pageable) {
        log.info("Fetching seats (page {}, size {})", pageable.getPageNumber(), pageable.getPageSize());
        Page<SeatResponseDto> seats = seatRepository.findAll(pageable).map(this::toDto);
        log.info("Found {} seats (page {} of {})", seats.getNumberOfElements(), seats.getNumber() + 1, seats.getTotalPages());
        return seats;
    }

    /**
     * @throws SeatNotFoundException if no seat exists with the given id
     */
    public SeatResponseDto getSeatById(Long seatId) {
        log.info("Fetching seat with id {}", seatId);
        Seat seat = seatRepository
                .findById(seatId)
                .orElseThrow(() -> {
                    log.warn("No seat found with id {}", seatId);
                    return new SeatNotFoundException("No seat found with id " + seatId);
                });
        return toDto(seat);
    }

    /**
     * Full seat map for a screen, used to render the seat-selection grid. Not
     * paginated: the frontend needs the whole map at once to lay out the grid.
     */
    public List<SeatResponseDto> getSeatsByScreen(Long screenId) {
        log.info("Fetching seats for screen with id {}", screenId);
        List<SeatResponseDto> seats = seatRepository.findByScreen_IdOrderByRowLabelAscSeatNumberAsc(screenId).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} seats for screen with id {}", seats.size(), screenId);
        return seats;
    }

    /**
     * Filtering the seat map by category (e.g. only PREMIUM seats) for
     * pricing/display. Not paginated, for the same reason as {@link #getSeatsByScreen}.
     */
    public List<SeatResponseDto> getSeatsByScreenAndType(Long screenId, SeatType seatType) {
        log.info("Fetching {} seats for screen with id {}", seatType, screenId);
        List<SeatResponseDto> seats = seatRepository
                .findByScreen_IdAndSeatTypeOrderByRowLabelAscSeatNumberAsc(screenId, seatType).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} {} seats for screen with id {}", seats.size(), seatType, screenId);
        return seats;
    }

    private SeatResponseDto toDto(Seat seat) {
        return modelMapper.map(seat, SeatResponseDto.class);
    }
}
