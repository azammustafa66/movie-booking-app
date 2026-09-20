package demo.bookingservice.service;

import demo.bookingservice.clients.CatalogClient;
import demo.bookingservice.dtos.BookingResponseDto;
import demo.bookingservice.dtos.CreateBookingRequestDto;
import demo.bookingservice.dtos.SeatMatrixDto;
import demo.bookingservice.dtos.SeatMatrixResponse;
import demo.bookingservice.dtos.internal.CatalogShowSeatMatrixResponse;
import demo.bookingservice.dtos.internal.SeatValidationRequest;
import demo.bookingservice.dtos.internal.ShowSeatValidationResponse;
import demo.bookingservice.dtos.internal.ValidatedSeatDto;
import demo.bookingservice.entities.Booking;
import demo.bookingservice.entities.BookingSeat;
import demo.bookingservice.entities.ShowSeat;
import demo.bookingservice.enums.BookingStatus;
import demo.bookingservice.enums.ShowSeatStatus;
import demo.bookingservice.exceptions.BookingNotFoundException;
import demo.bookingservice.exceptions.InvalidBookingStateException;
import demo.bookingservice.exceptions.SeatUnavailableException;
import demo.bookingservice.messaging.BookingEventProducer;
import demo.bookingservice.messaging.dto.BookingNotificationEvent;
import demo.bookingservice.messaging.event.NotificationEventType;
import demo.bookingservice.repos.BookingRepository;
import demo.bookingservice.repos.ShowSeatRepository;
import demo.bookingservice.security.AuthContextHolder;
import demo.bookingservice.security.AuthenticatedUser;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Owns the booking lifecycle: checkout, seat-matrix display, confirmation,
 * cancellation, and (via {@link #expireBooking}) expiry. Seat availability
 * is booking-service's own state — {@link ShowSeat} rows keyed by
 * {@code (showId, seatId)} — kept separate from catalog-service, which only
 * knows a seat's static shape (row/number/type/price), never whether it's
 * currently held. A lock is a {@code ShowSeatStatus.LOCKED} row with a
 * {@code lockedUntil} timestamp (see {@link #lockSeat}); reclaiming an
 * expired one happens two ways — lazily, the next time someone tries to
 * lock that same seat, or proactively, via
 * {@link demo.bookingservice.service.BookingExpiryService}'s background sweep.
 * Confirmation and cancellation each publish a
 * {@link demo.bookingservice.messaging.dto.BookingNotificationEvent} via
 * {@link demo.bookingservice.messaging.BookingEventProducer} for
 * notification-service to act on — checkout and expiry don't, since
 * neither is a customer-driven action worth notifying about the same way.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final ShowSeatRepository showSeatRepository;
    private final CatalogClient catalogClient;
    private final BookingRepository bookingRepository;
    private final BookingEventProducer bookingEventProducer;

    /**
     * Validates the requested seats against catalog-service, locks each one
     * for {@code seatId} (see {@link #lockSeat}), and creates a
     * {@code PENDING} booking priced from catalog-service's own per-seat
     * prices — never from anything the client sent. If any seat is
     * unavailable, {@link #lockSeat} throws {@link SeatUnavailableException}
     * and the whole checkout rolls back with none of the seats held.
     */
    @Transactional
    public BookingResponseDto checkout(CreateBookingRequestDto request) {

        Long userId = AuthContextHolder.getCurrentUser().userId();

        ShowSeatValidationResponse validation =
                catalogClient.validateSeats(
                        request.showId(),
                        new SeatValidationRequest(request.seatIds())
                );

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowId(request.showId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(expiresAt);

        BigDecimal totalPrice = validation.seats().stream()
                .map(ValidatedSeatDto::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        booking.setTotalAmount(totalPrice);

        Booking savedBooking = bookingRepository.save(booking);

        for (Long seatId : request.seatIds()) {
            lockSeat(
                    request.showId(),
                    seatId,
                    savedBooking.getId(),
                    expiresAt
            );
        }

        for (ValidatedSeatDto validatedSeat : validation.seats()) {

            BookingSeat bookingSeat = new BookingSeat();

            bookingSeat.setBooking(savedBooking);
            bookingSeat.setSeatId(validatedSeat.seatId());
            bookingSeat.setShowId(request.showId());
            bookingSeat.setPrice(validatedSeat.price());

            savedBooking.getBookingSeats().add(bookingSeat);
        }

        return new BookingResponseDto(
                savedBooking.getId(),
                savedBooking.getShowId(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                savedBooking.getExpiresAt()
        );
    }

    /**
     * The show's full seat map — catalog-service's static seat shapes
     * merged with this service's own live lock/booking status per seat.
     */
    @Transactional
    public SeatMatrixResponse getSeatMatrix(Long showId) {

        CatalogShowSeatMatrixResponse catalogResponse = catalogClient.getSeatMatrix(showId);

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);

        Map<Long, ShowSeat> statusBySeatId = showSeats
                .stream()
                .collect(Collectors.toMap(
                        ShowSeat::getSeatId, Function.identity()
                ));

        List<SeatMatrixDto> seats = catalogResponse.seats().stream().map(seat -> {
            // No row yet means no one has ever tried to lock this seat for this show —
            // that's exactly what AVAILABLE means, so there's nothing to backfill here.
            ShowSeat showSeat = statusBySeatId.get(seat.seatId());
            ShowSeatStatus status = showSeat != null ? showSeat.getStatus() : ShowSeatStatus.AVAILABLE;

            return new SeatMatrixDto(
                    seat.seatId(),
                    seat.rowLabel(),
                    seat.seatNumber(),
                    seat.seatType(),
                    seat.price(),
                    status
            );
        }).toList();

        return new SeatMatrixResponse(
                catalogResponse.showId(),
                catalogResponse.screenId(),
                seats
        );
    }

    /**
     * Cancels one of the caller's own {@code PENDING} or {@code CONFIRMED}
     * bookings and releases each of its seats back to {@code AVAILABLE}
     * (see {@link #releaseSeat}). Scoped to {@code userId} via
     * {@link BookingRepository#findByIdAndUserId} — a booking id that
     * exists but belongs to someone else looks identical to one that
     * doesn't exist at all, both surface as {@link BookingNotFoundException}.
     * Publishes a {@link NotificationEventType#BOOKING_CANCELLED} event once
     * the cancellation is applied, so notification-service can tell the
     * customer.
     *
     * @throws BookingNotFoundException     if no such booking exists for the caller
     * @throws InvalidBookingStateException if the booking is already {@code CANCELLED} or {@code EXPIRED}
     */
    @Transactional
    public void cancelBooking(Long bookingId) {
        AuthenticatedUser currentUser = AuthContextHolder.getCurrentUser();
        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, currentUser.userId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with Id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING &&  booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException("Booking cannot be cancelled");
        }

        for (BookingSeat seat : booking.getBookingSeats()) {
            releaseSeat(bookingId, seat.getShowId(), seat.getSeatId());
        }

        booking.setStatus(BookingStatus.CANCELLED);

        bookingEventProducer.publish(new BookingNotificationEvent(
                NotificationEventType.BOOKING_CANCELLED,
                booking.getId(),
                currentUser.userId(),
                booking.getShowId(),
                currentUser.email()
        ));
    }

    /**
     * Turns a {@code PENDING} booking into {@code CONFIRMED}, flipping each
     * of its seats from {@code LOCKED} to {@code BOOKED}. Re-checks every
     * seat against {@link ShowSeat} rather than trusting the booking's own
     * state — a seat could in principle have been released or reassigned
     * out from under this booking (e.g. by {@link #expireBooking} racing a
     * late confirmation), so a mismatch fails loudly instead of silently
     * confirming a booking that no longer actually holds its seats.
     * Publishes a {@link NotificationEventType#BOOKING_CONFIRMED} event once
     * confirmed, so notification-service can tell the customer.
     *
     * @throws BookingNotFoundException      if no such booking exists for the caller
     * @throws InvalidBookingStateException  if the booking isn't {@code PENDING} (already confirmed/cancelled/expired),
     *                                        or its hold has already lapsed — the latter also marks it {@code EXPIRED}
     * @throws SeatUnavailableException      if any of the booking's seats is no longer locked for it
     */
    @Transactional
    public BookingResponseDto confirmBooking(Long bookingId) {
        AuthenticatedUser currentUser = AuthContextHolder.getCurrentUser();

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, currentUser.userId()).orElseThrow(() -> new BookingNotFoundException("Booking not found with Id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingStateException("Booking cannot be confirmed");
        }

        LocalDateTime now = LocalDateTime.now();

        if (booking.getExpiresAt() == null || !booking.getExpiresAt().isAfter(now)) {
            booking.setStatus(BookingStatus.EXPIRED);
            throw new InvalidBookingStateException("Booking has expired");
        }

        List<ShowSeat> showSeats = new ArrayList<>();

        for (BookingSeat bookingSeat : booking.getBookingSeats()) {

            ShowSeat showSeat = showSeatRepository
                    .findByShowIdAndSeatId(
                            booking.getShowId(),
                            bookingSeat.getSeatId()
                    )
                    .orElseThrow(() ->
                            new SeatUnavailableException("Seat does not exist"));

            if (showSeat.getStatus() != ShowSeatStatus.LOCKED
                    || !booking.getId().equals(showSeat.getBookingId())) {

                throw new SeatUnavailableException(
                        "Seat is no longer locked for this booking"
                );
            }

            showSeats.add(showSeat);
        }

        for (ShowSeat showSeat : showSeats) {
            showSeat.setStatus(ShowSeatStatus.BOOKED);
            showSeat.setLockedUntil(null);
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        bookingEventProducer.publish(new BookingNotificationEvent(
                NotificationEventType.BOOKING_CONFIRMED,
                booking.getId(),
                currentUser.userId(),
                booking.getShowId(),
                currentUser.email()
        ));

        return new BookingResponseDto(
                booking.getId(),
                booking.getShowId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getExpiresAt()
        );
    }

    /**
     * Expires one {@code PENDING} booking whose hold has lapsed, releasing
     * its seats back to {@code AVAILABLE}. Called by
     * {@link BookingExpiryService}'s scheduled sweep, so unlike the public
     * controller-facing methods this isn't scoped to a caller — there is no
     * caller, it runs on a timer. Silently no-ops (rather than throwing) if
     * the booking has already moved on from {@code PENDING} or hasn't
     * actually expired yet, since the sweep's query can be marginally stale
     * by the time each booking is processed.
     *
     * @throws BookingNotFoundException if {@code bookingId} doesn't exist at all
     */
    @Transactional
    public void expireBooking(Long bookingId) {

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }

        if (booking.getExpiresAt() != null
                && booking.getExpiresAt().isAfter(LocalDateTime.now())) {
            return;
        }

        for (BookingSeat bookingSeat : booking.getBookingSeats()) {
            releaseSeat(
                    booking.getId(),
                    booking.getShowId(),
                    bookingSeat.getSeatId()
            );
        }

        booking.setStatus(BookingStatus.EXPIRED);
    }

    /**
     * The inverse of {@link #lockSeat}: puts one seat back to
     * {@code AVAILABLE}, clearing its lock. Requires an existing
     * {@link ShowSeat} row — unlike {@link #lockSeat}, there's no
     * create-on-demand case, since a seat being released must already have
     * been locked for this exact {@code bookingId}.
     *
     * @throws SeatUnavailableException     if the seat has no {@link ShowSeat} row at all
     * @throws InvalidBookingStateException if the seat is currently held by a different booking
     */
    private void releaseSeat(Long bookingId, Long showId, Long seatId) {
        ShowSeat seat = showSeatRepository
                .findByShowIdAndSeatId(showId, seatId)
                .orElseThrow(() -> new SeatUnavailableException("Seat does not exist for the show"));

        if (!bookingId.equals(seat.getBookingId())) {
            throw new InvalidBookingStateException("Seat does not belong to the booking");
        }

        seat.setStatus(ShowSeatStatus.AVAILABLE);
        seat.setBookingId(null);
        seat.setLockedUntil(null);
    }

    /**
     * Locks one seat for a show, creating its {@link ShowSeat} row on first
     * touch. There's no separate matrix-initialization step — catalog-service
     * already confirmed {@code seatId} belongs to this show's screen (via
     * {@code validateSeats} in {@link #checkout}), so a missing row here just
     * means no one has locked or booked this seat before, i.e. it's
     * available. {@link ShowSeatRepository#findByShowIdAndSeatId} takes a
     * pessimistic write lock on an existing row to serialize concurrent
     * bookings of the same seat; a genuinely new row has nothing to race
     * against.
     */
    private void lockSeat(Long showId, Long seatId, Long bookingId, LocalDateTime expiresAt) {
        ShowSeat seat = showSeatRepository
                .findByShowIdAndSeatId(showId, seatId)
                .orElseGet(() -> {
                    ShowSeat newSeat = new ShowSeat();
                    newSeat.setShowId(showId);
                    newSeat.setSeatId(seatId);
                    newSeat.setStatus(ShowSeatStatus.AVAILABLE);
                    return newSeat;
                });

        LocalDateTime now = LocalDateTime.now();

        if (seat.getStatus() == ShowSeatStatus.BOOKED) {
            throw new SeatUnavailableException("Seat is already booked");
        }

        if (seat.getStatus() == ShowSeatStatus.LOCKED
                && seat.getLockedUntil() != null
                && seat.getLockedUntil().isAfter(now)) {
            throw new SeatUnavailableException("Seat is currently locked");
        }

        // AVAILABLE OR EXPIRED LOCK
        seat.setStatus(ShowSeatStatus.LOCKED);
        seat.setBookingId(bookingId);
        seat.setLockedUntil(expiresAt);
        showSeatRepository.save(seat);
    }
}
