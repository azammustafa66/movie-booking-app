package demo.catalogservice.controller.internal;

import demo.catalogservice.dto.internal.CatalogShowSeatMatrixResponse;
import demo.catalogservice.dto.internal.SeatValidationRequest;
import demo.catalogservice.dto.internal.ShowSeatValidationResponse;
import demo.catalogservice.service.internal.CatalogValidationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Service-to-service catalog API, called by booking-service (via its
 * {@code CatalogClient} Feign client) to validate a seat selection before a
 * booking is created.
 * <p>
 * Unlike {@code /api/v1/admin/**} and {@code /api/v1/vendor/**}, routes here
 * are <strong>not</strong> covered by {@code AuthInterceptor} (see
 * {@code demo.catalogservice.config.SecurityConfig}) — there's no caller
 * identity to check, since the caller is another service, not a user. That
 * makes this endpoint reachable by anything that can resolve
 * {@code catalog-service} on the network, so it must stay unreachable from
 * outside the cluster (no gateway route, no public ingress) rather than
 * relying on request-level auth.
 */
@RestController
@RequestMapping("/internal/catalog")
@RequiredArgsConstructor
@Validated
public class InternalCatalogController {

    private final CatalogValidationService catalogValidationService;

    /**
     * Confirms every requested seat belongs to the given show's screen,
     * and returns each seat's type so booking-service can price the
     * booking without needing its own copy of the seat map.
     */
    @PostMapping("/shows/{showId}/validate-seats")
    public ResponseEntity<ShowSeatValidationResponse> validateSeats(
            @PathVariable
            @Positive(message = "Show ID must be greater than 0")
            Long showId,

            @Valid
            @RequestBody
            SeatValidationRequest request
    ) {
        return ResponseEntity.ok(
                catalogValidationService.validateSeats(showId, request)
        );
    }

    /**
     * The full seat map for a show's screen — booking-service renders its
     * seat-picker from this and lazily initializes its own per-show
     * seat-status rows the first time a seat on this show is touched.
     */
    @GetMapping("/shows/{showId}/seat-matrix")
    public ResponseEntity<CatalogShowSeatMatrixResponse> getSeatMatrix(
            @PathVariable
            @Positive(message = "Show ID must be greater than 0")
            Long showId
    ) {
        return ResponseEntity.ok(
                catalogValidationService.getSeatMatrix(showId)
        );
    }
}