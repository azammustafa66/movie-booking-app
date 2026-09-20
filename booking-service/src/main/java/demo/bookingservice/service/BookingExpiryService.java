package demo.bookingservice.service;

import demo.bookingservice.entities.Booking;
import demo.bookingservice.enums.BookingStatus;
import demo.bookingservice.exceptions.BookingNotFoundException;
import demo.bookingservice.repos.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background sweep that reclaims seats held by abandoned checkouts. A
 * {@code PENDING} booking's seats stay {@code LOCKED} until either
 * {@link BookingService#confirmBooking} confirms them or this sweep expires
 * them — without it, a customer who checks out but never confirms would
 * hold those seats forever, since nothing else revisits a {@code PENDING}
 * booking on its own.
 */
@Service
@RequiredArgsConstructor
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * Runs every 30 seconds (measured from the end of the previous run, not
     * a fixed clock tick — see {@code fixedDelay}), expiring every
     * {@code PENDING} booking whose {@code expiresAt} has already passed.
     * There's no per-booking error isolation: if {@link BookingService#expireBooking}
     * throws partway through, the rest of this run's batch is skipped, but
     * since the query re-runs fresh each cycle, those bookings are simply
     * picked up again on the next tick.
     */
    @Scheduled(fixedDelay = 30_000)
    public void expireBookings() {
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, LocalDateTime.now()
        );

        for (Booking expiredBooking : expiredBookings) {
            bookingService.expireBooking(expiredBooking.getId());
        }
    }
}