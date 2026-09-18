package demo.catalogservice.controller;

import demo.catalogservice.dto.ScreenResponseDto;
import demo.catalogservice.service.ScreenService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only endpoints over theatre screens. */
@RestController
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ScreenController {

    private final ScreenService screenService;

    /** All screens across every theatre. */
    @GetMapping
    public ResponseEntity<List<ScreenResponseDto>> getAllScreens() {
        List<ScreenResponseDto> screens = screenService.getScreens();
        return ResponseEntity.ok(screens);
    }

    /** Fetches a single screen by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{screenId}")
    public ResponseEntity<ScreenResponseDto> getScreenById(
            @PathVariable @Positive(message = "Screen ID cannot be less than 0") Long screenId) {
        ScreenResponseDto screen = screenService.getScreenById(screenId);
        return ResponseEntity.ok(screen);
    }

    /** All screens belonging to a theatre, e.g. for that theatre's own listing page. */
    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<List<ScreenResponseDto>> getScreensByTheatre(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId) {
        List<ScreenResponseDto> screens = screenService.getScreensByTheatre(theatreId);
        return ResponseEntity.ok(screens);
    }
}
