package demo.catalogservice.controller;

import demo.catalogservice.dto.TheatreResponseDto;
import demo.catalogservice.service.TheatreService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only browse/search endpoints over theatres.
 * <p>
 * Every paginated endpoint here accepts {@code ?sort=<property>,<asc|desc>}
 * (repeatable for a multi-key sort). Valid properties are any {@code
 * Theatre} field: {@code name}, {@code city}, {@code state}, {@code
 * address}, {@code pincode}.
 */
@RestController
@RequestMapping("/api/v1/theatres")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TheatreController {

    private final TheatreService theatreService;

    /** All theatres in the catalog. */
    @GetMapping
    public ResponseEntity<PagedModel<TheatreResponseDto>> getAllTheatres(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TheatreResponseDto> theatres = theatreService.getTheatres(pageable);
        return ResponseEntity.ok(new PagedModel<>(theatres));
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
    public ResponseEntity<PagedModel<TheatreResponseDto>> getTheatresByCity(
            @PathVariable String city,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TheatreResponseDto> theatres = theatreService.getTheatresByCity(city, pageable);
        return ResponseEntity.ok(new PagedModel<>(theatres));
    }

    /** Search-bar lookup as the user types a theatre name. */
    @GetMapping("/search")
    public ResponseEntity<PagedModel<TheatreResponseDto>> getTheatresByName(
            @NotBlank(message = "Theatre name cannot be blank") @RequestParam("name") String name,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TheatreResponseDto> theatres = theatreService.getTheatresByName(name, pageable);
        return ResponseEntity.ok(new PagedModel<>(theatres));
    }
}
