package demo.catalogservice.dto.response;

import demo.catalogservice.enums.ScreenType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Public-facing view of a {@link demo.catalogservice.entities.Screen}.
 * <p>
 * {@code theatreId}/{@code theatreName} are flattened from the parent
 * {@code Theatre} (ModelMapper resolves these from {@code theatre.id}/
 * {@code theatre.name} automatically) so the response never nests the full
 * {@code Theatre} entity, and {@code seats} is excluded — fetched separately
 * via {@code GET /api/v1/seats/screen/{screenId}}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScreenResponseDto {
    private Long id;
    private String name;
    private Long theatreId;
    private String theatreName;
    private Integer capacity;
    private ScreenType screenType;
}
