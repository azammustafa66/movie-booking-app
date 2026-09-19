package demo.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for admin create/update of a {@link demo.catalogservice.entities.Genre}.
 * Admin-only: genres are shared catalog data, not owned by any vendor.
 */
@Getter
@Setter
public class GenreRequestDto {

    @NotBlank(message = "Genre name cannot be blank")
    private String name;
}
