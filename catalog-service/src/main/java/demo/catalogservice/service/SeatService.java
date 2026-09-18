package demo.catalogservice.service;

import demo.catalogservice.dto.SeatResponseDto;
import demo.catalogservice.entities.Seat;
import demo.catalogservice.enums.SeatType;
import demo.catalogservice.exceptions.SeatNotFoundException;
import demo.catalogservice.repos.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
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
    public List<SeatResponseDto> getSeats() {
        log.info("Fetching all seats");
        List<SeatResponseDto> seats = seatRepository.findAll().stream().map(this::toDto).toList();
        log.info("Found {} seats", seats.size());
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

    /** Full seat map for a screen, used to render the seat-selection grid. */
    public List<SeatResponseDto> getSeatsByScreen(Long screenId) {
        log.info("Fetching seats for screen with id {}", screenId);
        List<SeatResponseDto> seats = seatRepository.findByScreen_Id(screenId).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} seats for screen with id {}", seats.size(), screenId);
        return seats;
    }

    /** Filtering the seat map by category (e.g. only PREMIUM seats) for pricing/display. */
    public List<SeatResponseDto> getSeatsByScreenAndType(Long screenId, SeatType seatType) {
        log.info("Fetching {} seats for screen with id {}", seatType, screenId);
        List<SeatResponseDto> seats = seatRepository.findByScreen_IdAndSeatType(screenId, seatType).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} {} seats for screen with id {}", seats.size(), seatType, screenId);
        return seats;
    }

    private SeatResponseDto toDto(Seat seat) {
        return modelMapper.map(seat, SeatResponseDto.class);
    }
}
