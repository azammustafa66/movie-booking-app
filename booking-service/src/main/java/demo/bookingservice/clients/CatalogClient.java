package demo.bookingservice.clients;

import demo.bookingservice.dtos.internal.CatalogShowSeatMatrixResponse;
import demo.bookingservice.dtos.internal.SeatValidationRequest;
import demo.bookingservice.dtos.internal.ShowSeatValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Declarative Feign client for catalog-service's internal (service-to-service,
 * unauthenticated) API — resolved via Eureka by the {@code catalog-service}
 * application name, not a hardcoded host. Both endpoints live under
 * {@code /internal/catalog}, deliberately not routed through the gateway.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    /**
     * Confirms every seat id belongs to the show's screen and returns each
     * one's price, so booking-service never has to trust — or store — a
     * price the client sent.
     *
     * @throws feign.FeignException.NotFound     if the show doesn't exist (catalog-service's 404)
     * @throws feign.FeignException.BadRequest   if any seat id isn't on the show's screen (catalog-service's 400)
     */
    @PostMapping("/internal/catalog/shows/{showId}/validate-seats")
    ShowSeatValidationResponse validateSeats(
            @PathVariable Long showId,
            @RequestBody SeatValidationRequest request
    );

    /** The show's full static seat map (shape and price only, no availability — see {@link #validateSeats}). */
    @GetMapping("/internal/catalog/shows/{showId}/seat-matrix")
    CatalogShowSeatMatrixResponse getSeatMatrix(@PathVariable Long showId);
}
