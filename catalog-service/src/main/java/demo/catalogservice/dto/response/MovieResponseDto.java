package demo.catalogservice.dto.response;

import demo.catalogservice.enums.Certification;
import demo.catalogservice.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * Public-facing view of a {@link demo.catalogservice.entities.Movie}.
 * <p>
 * Genres are flattened to their names so the API never has to serialize the
 * {@code Genre -> movies} back-reference, which would otherwise recurse
 * infinitely through the bidirectional many-to-many mapping.
 * <p>
 * A mutable POJO (rather than a record) so {@link org.modelmapper.ModelMapper}
 * can instantiate and populate it via its no-arg constructor and setters.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {
    private Long id;
    private String title;
    private String description;
    private String language;
    private LocalDate releaseDate;
    private Certification certification;
    private Integer durationInMinutes;
    private String posterUrl;
    private MovieStatus status;
    private Set<String> genres;
}
