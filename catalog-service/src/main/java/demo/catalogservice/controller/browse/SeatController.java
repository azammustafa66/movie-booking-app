package demo.catalogservice.controller.browse;

import demo.catalogservice.dto.response.SeatResponseDto;
import demo.catalogservice.enums.SeatType;
import demo.catalogservice.service.browse.SeatService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only endpoints over the seat map.
 * <p>
 * {@link #getAllSeats} accepts {@code ?sort=<property>,<asc|desc>}
 * (repeatable for a multi-key sort). Valid properties are any {@code Seat}
 * field: {@code rowLabel}, {@code seatNumber}, {@code seatType}. The
 * per-screen endpoints below are always returned in row-major reading order
 * ({@code rowLabel}, then {@code seatNumber}) since that's what a seat grid
 * needs, and aren't independently sortable.
 */
@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
@Validated
@Slf4j
public class SeatController {

    private final SeatService seatService;

    /** All seats across every screen. */
    @GetMapping
    public ResponseEntity<PagedModel<SeatResponseDto>> getAllSeats(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SeatResponseDto> seats = seatService.getSeats(pageable);
        return ResponseEntity.ok(new PagedModel<>(seats));
    }

    /** Fetches a single seat by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponseDto> getSeatById(
            @PathVariable @Positive(message = "Seat ID cannot be less than 0") Long seatId) {
        SeatResponseDto seat = seatService.getSeatById(seatId);
        return ResponseEntity.ok(seat);
    }

    /** Full seat map for a screen, used to render the seat-selection grid. */
    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponseDto>> getSeatsByScreen(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId) {
        List<SeatResponseDto> seats = seatService.getSeatsByScreen(screenId);
        return ResponseEntity.ok(seats);
    }

    /** Filtering the seat map by category (e.g. only PREMIUM seats) for pricing/display. */
    @GetMapping("/screen/{screenId}/type/{seatType}")
    public ResponseEntity<List<SeatResponseDto>> getSeatsByScreenAndType(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId,
            @PathVariable SeatType seatType) {
        List<SeatResponseDto> seats = seatService.getSeatsByScreenAndType(screenId, seatType);
        return ResponseEntity.ok(seats);
    }
}
