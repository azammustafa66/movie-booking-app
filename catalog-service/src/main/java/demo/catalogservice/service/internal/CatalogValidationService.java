package demo.catalogservice.service.internal;

import demo.catalogservice.dto.internal.CatalogSeatDto;
import demo.catalogservice.dto.internal.CatalogShowSeatMatrixResponse;
import demo.catalogservice.dto.internal.SeatValidationRequest;
import demo.catalogservice.dto.internal.ShowSeatValidationResponse;
import demo.catalogservice.dto.internal.ValidatedSeatDto;
import demo.catalogservice.entities.Seat;
import demo.catalogservice.entities.Show;
import demo.catalogservice.exceptions.InvalidRequestException;
import demo.catalogservice.exceptions.ShowNotFoundException;
import demo.catalogservice.repos.SeatRepository;
import demo.catalogservice.repos.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backs {@link demo.catalogservice.controller.internal.InternalCatalogController},
 * answering booking-service's questions about a show's seats: which ones a
 * requested selection actually resolves to ({@link #validateSeats}), and the
 * full seat map for rendering a seat-picker ({@link #getSeatMatrix}).
 * Availability (locked/booked) is never part of this answer — that's state
 * booking-service owns, not catalog-service.
 */
@Service
@RequiredArgsConstructor
public class CatalogValidationService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    /**
     * Validates that every seat id in {@code request} belongs to the show's
     * screen. Seat availability (already booked or not) is deliberately out
     * of scope here — that's booking-service's own concern once it holds
     * the seats, not something catalog-service tracks.
     *
     * @throws demo.catalogservice.exceptions.ShowNotFoundException  if {@code showId} doesn't exist
     * @throws demo.catalogservice.exceptions.InvalidRequestException if any seat id isn't on the show's screen
     */
    @Transactional(readOnly = true)
    public ShowSeatValidationResponse validateSeats(
            Long showId,
            SeatValidationRequest request
    ) {
        Show show = showRepository.findWithScreenById(showId)
                .orElseThrow(() ->
                        new ShowNotFoundException(
                                "No show found with id " + showId
                        )
                );

        Long screenId = show.getScreen().getId();

        List<Long> seatIds = request.seatIds();

        List<Seat> seats =
                seatRepository.findByScreen_IdAndIdIn(screenId, seatIds);

        if (seats.size() != seatIds.size()) {
            throw new InvalidRequestException(
                    "One or more seats do not belong to show " + showId
            );
        }

        List<ValidatedSeatDto> validatedSeats = seats.stream()
                .map(seat -> new ValidatedSeatDto(
                        seat.getId(),
                        seat.getSeatType(),
                        seat.getPrice()
                ))
                .toList();

        return new ShowSeatValidationResponse(
                show.getId(),
                screenId,
                validatedSeats
        );
    }

    /**
     * The full seat map for a show's screen, for booking-service to render a
     * seat-picker and to lazily initialize its own per-show seat-status rows
     * against.
     *
     * @throws ShowNotFoundException if {@code showId} doesn't exist
     */
    @Transactional(readOnly = true)
    public CatalogShowSeatMatrixResponse getSeatMatrix(Long showId) {
        Show show = showRepository.findWithScreenById(showId)
                .orElseThrow(() ->
                        new ShowNotFoundException(
                                "No show found with id " + showId
                        )
                );

        Long screenId = show.getScreen().getId();

        List<CatalogSeatDto> seats = seatRepository
                .findByScreen_IdOrderByRowLabelAscSeatNumberAsc(screenId)
                .stream()
                .map(seat -> new CatalogSeatDto(
                        seat.getId(),
                        seat.getRowLabel(),
                        seat.getSeatNumber(),
                        seat.getSeatType(),
                        seat.getPrice()
                ))
                .toList();

        return new CatalogShowSeatMatrixResponse(show.getId(), screenId, seats);
    }
}