package demo.apigateway.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Landing points for the {@code CircuitBreaker} route filter's {@code fallbackPath}
 * (see application.yaml) — reached via an internal servlet forward when a downstream
 * service's circuit breaker is OPEN, so the client gets a clean, predictable error body
 * instead of a raw connection-refused/timeout leaking through the gateway.
 * <p>
 * Unrestricted by HTTP method ({@code @RequestMapping} with no {@code method}): a servlet
 * forward preserves the *original* request's method, and user-service's routes are all
 * {@code POST} — an {@code @GetMapping} here would never actually match.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/user-service")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "user-service is temporarily unavailable",
                        "timestamp", Instant.now().toString()
                ));
    }

    @RequestMapping("/fallback/booking-service")
    public ResponseEntity<Map<String, Object>> bookingServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "booking-service is temporarily unavailable",
                        "timestamp", Instant.now().toString()
                ));
    }

    @RequestMapping("/fallback/catalog-service")
    public ResponseEntity<Map<String, Object>> catalogServiceFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "catalog-service is temporarily unavailable",
                        "timestamp", Instant.now().toString()
                ));
    }
}
