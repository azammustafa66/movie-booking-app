package demo.catalogservice.controller.vendor;

import demo.catalogservice.controller.admin.AdminController;
import demo.catalogservice.dto.response.ScreenResponseDto;
import demo.catalogservice.dto.response.SeatResponseDto;
import demo.catalogservice.dto.response.ShowResponseDto;
import demo.catalogservice.dto.response.TheatreResponseDto;
import demo.catalogservice.dto.request.ScreenRequestDto;
import demo.catalogservice.dto.request.SeatRequestDto;
import demo.catalogservice.dto.request.ShowRequestDto;
import demo.catalogservice.dto.request.VendorTheatreRequestDto;
import demo.catalogservice.security.AuthContextHolder;
import demo.catalogservice.security.AuthenticatedUser;
import demo.catalogservice.service.vendor.VendorCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Write-side catalog management for vendors, scoped to what they own.
 * <p>
 * Every route here requires {@code X-User-Role: VENDOR}, enforced by
 * {@code AuthInterceptor} before any request reaches this controller (see
 * {@code demo.catalogservice.config.SecurityConfig}). The caller's identity
 * comes from {@link AuthContextHolder#getCurrentUser()} — a vendor can
 * create theatres for themselves and manage screens/seats/shows under them,
 * but touching a theatre (or anything nested under one) owned by a
 * different vendor fails with {@code 403}, enforced by
 * {@link VendorCatalogService}. Movies and genres aren't vendor-owned, so
 * there's nothing for a vendor to manage there — see {@link AdminController}
 * for those.
 */
@RestController
@RequestMapping("/api/v1/vendor")
@RequiredArgsConstructor
@Validated
@Slf4j
public class VendorController {

    private final VendorCatalogService vendorCatalogService;

    // ------------------------------------------------------------------
    // Theatres
    // ------------------------------------------------------------------

    /** The calling vendor's own theatres. */
    @GetMapping("/theatres")
    public ResponseEntity<PagedModel<TheatreResponseDto>> getMyTheatres(
            @PageableDefault(size = 20) Pageable pageable) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        Page<TheatreResponseDto> theatres = vendorCatalogService.getMyTheatres(vendor.userId(), pageable);
        return ResponseEntity.ok(new PagedModel<>(theatres));
    }

    /** Creates a new theatre owned by the calling vendor; {@code vendorId} is always the caller, never client-supplied. */
    @PostMapping("/theatres")
    public ResponseEntity<TheatreResponseDto> createTheatre(@Valid @RequestBody VendorTheatreRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        TheatreResponseDto theatre = vendorCatalogService.createTheatre(vendor.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(theatre);
    }

    @PutMapping("/theatres/{theatreId}")
    public ResponseEntity<TheatreResponseDto> updateTheatre(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId,
            @Valid @RequestBody VendorTheatreRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        TheatreResponseDto theatre = vendorCatalogService.updateTheatre(vendor.userId(), theatreId, request);
        return ResponseEntity.ok(theatre);
    }

    @DeleteMapping("/theatres/{theatreId}")
    public ResponseEntity<Void> deleteTheatre(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        vendorCatalogService.deleteTheatre(vendor.userId(), theatreId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Screens
    // ------------------------------------------------------------------

    @PostMapping("/screens")
    public ResponseEntity<ScreenResponseDto> createScreen(@Valid @RequestBody ScreenRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        ScreenResponseDto screen = vendorCatalogService.createScreen(vendor.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(screen);
    }

    @PutMapping("/screens/{screenId}")
    public ResponseEntity<ScreenResponseDto> updateScreen(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId,
            @Valid @RequestBody ScreenRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        ScreenResponseDto screen = vendorCatalogService.updateScreen(vendor.userId(), screenId, request);
        return ResponseEntity.ok(screen);
    }

    @DeleteMapping("/screens/{screenId}")
    public ResponseEntity<Void> deleteScreen(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        vendorCatalogService.deleteScreen(vendor.userId(), screenId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Seats
    // ------------------------------------------------------------------

    @PostMapping("/seats")
    public ResponseEntity<SeatResponseDto> createSeat(@Valid @RequestBody SeatRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        SeatResponseDto seat = vendorCatalogService.createSeat(vendor.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @PutMapping("/seats/{seatId}")
    public ResponseEntity<SeatResponseDto> updateSeat(
            @PathVariable @Positive(message = "Seat ID cannot be less than 0") Long seatId,
            @Valid @RequestBody SeatRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        SeatResponseDto seat = vendorCatalogService.updateSeat(vendor.userId(), seatId, request);
        return ResponseEntity.ok(seat);
    }

    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable @Positive(message = "Seat ID cannot be less than 0") Long seatId) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        vendorCatalogService.deleteSeat(vendor.userId(), seatId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Shows
    // ------------------------------------------------------------------

    @PostMapping("/shows")
    public ResponseEntity<ShowResponseDto> createShow(@Valid @RequestBody ShowRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        ShowResponseDto show = vendorCatalogService.createShow(vendor.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(show);
    }

    @PutMapping("/shows/{showId}")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable @Positive(message = "Show ID cannot be less than 0") Long showId,
            @Valid @RequestBody ShowRequestDto request) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        ShowResponseDto show = vendorCatalogService.updateShow(vendor.userId(), showId, request);
        return ResponseEntity.ok(show);
    }

    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(
            @PathVariable @Positive(message = "Show ID cannot be less than 0") Long showId) {
        AuthenticatedUser vendor = AuthContextHolder.getCurrentUser();
        vendorCatalogService.deleteShow(vendor.userId(), showId);
        return ResponseEntity.noContent().build();
    }
}
