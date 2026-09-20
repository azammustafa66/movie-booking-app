package demo.bookingservice.controllers;

import demo.bookingservice.dtos.BookingResponseDto;
import demo.bookingservice.dtos.CreateBookingRequestDto;
import demo.bookingservice.dtos.SeatMatrixResponse;
import demo.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Customer/admin-facing booking APIs: browse a show's seat map, check out a
 * seat selection, confirm or cancel a booking. Every route here is guarded
 * by {@code AuthInterceptor} (see {@code demo.bookingservice.config.SecurityConfig}),
 * which allows {@code CUSTOMER} and {@code ADMIN} but rejects {@code VENDOR}
 * — vendors manage events through catalog-service, they don't attend them.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Slf4j
@Validated
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** The show's full seat map, each seat annotated with its live lock/booking status. */
    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<SeatMatrixResponse> getSeatMatrix(
            @PathVariable @Positive Long showId) {

        return ResponseEntity.ok(
                bookingService.getSeatMatrix(showId)
        );
    }

    /** Locks the requested seats and creates a {@code PENDING} booking for the caller. */
    @PostMapping
    public ResponseEntity<BookingResponseDto> checkout(@Valid @RequestBody CreateBookingRequestDto request) {
        return ResponseEntity.ok(bookingService.checkout(request));
    }

    /**
     * Cancels one of the caller's own bookings and releases its seats back
     * to {@code AVAILABLE}. Scoped to the caller's own bookings by
     * {@link BookingService#cancelBooking} — one customer can't cancel
     * another's booking by guessing an id.
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable @Positive Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirms a {@code PENDING} booking, converting its held seats to
     * {@code BOOKED}. Must happen before {@link BookingService#expireBooking}'s
     * background sweep reclaims the hold — see {@link BookingService#confirmBooking}
     * for exactly when that window closes.
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponseDto> confirmBooking(
            @PathVariable @Positive Long bookingId) {

        return ResponseEntity.ok(
                bookingService.confirmBooking(bookingId)
        );
    }
}
