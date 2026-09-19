package demo.catalogservice.dto.request;

import demo.catalogservice.enums.Certification;
import demo.catalogservice.enums.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Body for admin create/update of a {@link demo.catalogservice.entities.Movie}.
 * Admin-only: movies are shared catalog data, not owned by any vendor.
 */
@Getter
@Setter
public class MovieRequestDto {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Language cannot be blank")
    private String language;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    private Certification certification;

    @Positive(message = "Duration must be greater than 0")
    private Integer durationInMinutes;

    private String posterUrl;

    @NotNull(message = "Status is required")
    private MovieStatus status;

    /** IDs of existing {@link demo.catalogservice.entities.Genre} rows to tag this movie with; may be empty. */
    private Set<Long> genreIds = new HashSet<>();
}
