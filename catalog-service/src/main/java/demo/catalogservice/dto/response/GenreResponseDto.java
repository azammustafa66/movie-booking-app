package demo.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Public-facing view of a {@link demo.catalogservice.entities.Genre}. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponseDto {
    private Long id;
    private String name;
}
