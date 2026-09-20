package demo.catalogservice.controller.browse;

import demo.catalogservice.dto.response.GenreResponseDto;
import demo.catalogservice.service.browse.GenreService;
import jakarta.validation.constraints.NotBlank;
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

/** Read-only endpoints over movie genres. */
@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Validated
@Slf4j
public class GenreController {

    private final GenreService genreService;

    /** All genres, e.g. to populate a "filter by genre" dropdown. */
    @GetMapping
    public ResponseEntity<List<GenreResponseDto>> getAllGenres() {
        List<GenreResponseDto> genres = genreService.getGenres();
        return ResponseEntity.ok(genres);
    }

    /** Fetches a single genre by id, or {@code 404} if it doesn't exist. */
    @GetMapping("/{genreId}")
    public ResponseEntity<GenreResponseDto> getGenreById(
            @PathVariable @Positive(message = "Genre ID cannot be less than 0") Long genreId) {
        GenreResponseDto genre = genreService.getGenreById(genreId);
        return ResponseEntity.ok(genre);
    }

    /** Fetches a single genre by its exact name, or {@code 404} if it doesn't exist. */
    @GetMapping("/name/{name}")
    public ResponseEntity<GenreResponseDto> getGenreByName(
            @PathVariable @NotBlank(message = "Genre name cannot be blank") String name) {
        GenreResponseDto genre = genreService.getGenreByName(name);
        return ResponseEntity.ok(genre);
    }
}
