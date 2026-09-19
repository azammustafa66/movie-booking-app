package demo.catalogservice.controller;

import demo.catalogservice.dto.GenreResponseDto;
import demo.catalogservice.dto.MovieResponseDto;
import demo.catalogservice.dto.ScreenResponseDto;
import demo.catalogservice.dto.SeatResponseDto;
import demo.catalogservice.dto.ShowResponseDto;
import demo.catalogservice.dto.TheatreResponseDto;
import demo.catalogservice.dto.request.AdminTheatreRequestDto;
import demo.catalogservice.dto.request.GenreRequestDto;
import demo.catalogservice.dto.request.MovieRequestDto;
import demo.catalogservice.dto.request.ScreenRequestDto;
import demo.catalogservice.dto.request.SeatRequestDto;
import demo.catalogservice.dto.request.ShowRequestDto;
import demo.catalogservice.service.AdminCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Write-side catalog management for platform admins.
 * <p>
 * Every route here requires {@code X-User-Role: ADMIN}, enforced by
 * {@code AuthInterceptor} before any request reaches this
 * controller (see {@code demo.catalogservice.config.SecurityConfig}) — an
 * admin can create, update, or delete any theatre, screen, seat, show,
 * movie, or genre, with no ownership restriction. Read access to the same
 * data is already public via {@link MovieController}, {@link TheatreController},
 * etc., so this controller only exposes the mutating operations.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminController {

    private final AdminCatalogService adminCatalogService;

    // ------------------------------------------------------------------
    // Theatres
    // ------------------------------------------------------------------

    @PostMapping("/theatres")
    public ResponseEntity<TheatreResponseDto> createTheatre(@Valid @RequestBody AdminTheatreRequestDto request) {
        TheatreResponseDto theatre = adminCatalogService.createTheatre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(theatre);
    }

    @PutMapping("/theatres/{theatreId}")
    public ResponseEntity<TheatreResponseDto> updateTheatre(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId,
            @Valid @RequestBody AdminTheatreRequestDto request) {
        TheatreResponseDto theatre = adminCatalogService.updateTheatre(theatreId, request);
        return ResponseEntity.ok(theatre);
    }

    @DeleteMapping("/theatres/{theatreId}")
    public ResponseEntity<Void> deleteTheatre(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId) {
        adminCatalogService.deleteTheatre(theatreId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Screens
    // ------------------------------------------------------------------

    @PostMapping("/screens")
    public ResponseEntity<ScreenResponseDto> createScreen(@Valid @RequestBody ScreenRequestDto request) {
        ScreenResponseDto screen = adminCatalogService.createScreen(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(screen);
    }

    @PutMapping("/screens/{screenId}")
    public ResponseEntity<ScreenResponseDto> updateScreen(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId,
            @Valid @RequestBody ScreenRequestDto request) {
        ScreenResponseDto screen = adminCatalogService.updateScreen(screenId, request);
        return ResponseEntity.ok(screen);
    }

    @DeleteMapping("/screens/{screenId}")
    public ResponseEntity<Void> deleteScreen(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId) {
        adminCatalogService.deleteScreen(screenId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Seats
    // ------------------------------------------------------------------

    @PostMapping("/seats")
    public ResponseEntity<SeatResponseDto> createSeat(@Valid @RequestBody SeatRequestDto request) {
        SeatResponseDto seat = adminCatalogService.createSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(seat);
    }

    @PutMapping("/seats/{seatId}")
    public ResponseEntity<SeatResponseDto> updateSeat(
            @PathVariable @Positive(message = "Seat ID cannot be less than 0") Long seatId,
            @Valid @RequestBody SeatRequestDto request) {
        SeatResponseDto seat = adminCatalogService.updateSeat(seatId, request);
        return ResponseEntity.ok(seat);
    }

    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Void> deleteSeat(
            @PathVariable @Positive(message = "Seat ID cannot be less than 0") Long seatId) {
        adminCatalogService.deleteSeat(seatId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Shows
    // ------------------------------------------------------------------

    @PostMapping("/shows")
    public ResponseEntity<ShowResponseDto> createShow(@Valid @RequestBody ShowRequestDto request) {
        ShowResponseDto show = adminCatalogService.createShow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(show);
    }

    @PutMapping("/shows/{showId}")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable @Positive(message = "Show ID cannot be less than 0") Long showId,
            @Valid @RequestBody ShowRequestDto request) {
        ShowResponseDto show = adminCatalogService.updateShow(showId, request);
        return ResponseEntity.ok(show);
    }

    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<Void> deleteShow(
            @PathVariable @Positive(message = "Show ID cannot be less than 0") Long showId) {
        adminCatalogService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Movies
    // ------------------------------------------------------------------

    @PostMapping("/movies")
    public ResponseEntity<MovieResponseDto> createMovie(@Valid @RequestBody MovieRequestDto request) {
        MovieResponseDto movie = adminCatalogService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @PathVariable @Positive(message = "Movie ID cannot be less than 0") Long movieId,
            @Valid @RequestBody MovieRequestDto request) {
        MovieResponseDto movie = adminCatalogService.updateMovie(movieId, request);
        return ResponseEntity.ok(movie);
    }

    @DeleteMapping("/movies/{movieId}")
    public ResponseEntity<Void> deleteMovie(
            @PathVariable @Positive(message = "Movie ID cannot be less than 0") Long movieId) {
        adminCatalogService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Genres
    // ------------------------------------------------------------------

    @PostMapping("/genres")
    public ResponseEntity<GenreResponseDto> createGenre(@Valid @RequestBody GenreRequestDto request) {
        GenreResponseDto genre = adminCatalogService.createGenre(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(genre);
    }

    @PutMapping("/genres/{genreId}")
    public ResponseEntity<GenreResponseDto> updateGenre(
            @PathVariable @Positive(message = "Genre ID cannot be less than 0") Long genreId,
            @Valid @RequestBody GenreRequestDto request) {
        GenreResponseDto genre = adminCatalogService.updateGenre(genreId, request);
        return ResponseEntity.ok(genre);
    }

    @DeleteMapping("/genres/{genreId}")
    public ResponseEntity<Void> deleteGenre(
            @PathVariable @Positive(message = "Genre ID cannot be less than 0") Long genreId) {
        adminCatalogService.deleteGenre(genreId);
        return ResponseEntity.noContent().build();
    }
}
