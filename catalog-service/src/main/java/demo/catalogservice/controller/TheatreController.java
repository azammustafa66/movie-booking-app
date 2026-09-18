package demo.catalogservice.controller;

import demo.catalogservice.dto.TheatreResponseDto;
import demo.catalogservice.service.TheatreService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only browse/search endpoints over theatres. */
@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TheatreController {

    private final TheatreService theatreService;

    /** All theatres in the catalog. */
    @GetMapping
    public ResponseEntity<List<TheatreResponseDto>> getAllTheatres() {
        List<TheatreResponseDto> theatres = theatreService.getTheatres();
        return ResponseEntity.ok(theatres);
    }

    /** Fetches a single theatre by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{theatreId}")
    public ResponseEntity<TheatreResponseDto> getTheatreById(
            @PathVariable @Positive(message = "Theatre ID cannot be less than 0") Long theatreId) {
        TheatreResponseDto theatre = theatreService.getTheatreById(theatreId);
        return ResponseEntity.ok(theatre);
    }

    /** "Pick your city" step: theatres available in the city the user selected. */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<TheatreResponseDto>> getTheatresByCity(@PathVariable String city) {
        List<TheatreResponseDto> theatres = theatreService.getTheatresByCity(city);
        return ResponseEntity.ok(theatres);
    }

    /** Search-bar lookup as the user types a theatre name. */
    @GetMapping("/search")
    public ResponseEntity<List<TheatreResponseDto>> getTheatresByName(
            @NotBlank(message = "Theatre name cannot be blank") @RequestParam("name") String name) {
        List<TheatreResponseDto> theatres = theatreService.getTheatresByName(name);
        return ResponseEntity.ok(theatres);
    }
}
