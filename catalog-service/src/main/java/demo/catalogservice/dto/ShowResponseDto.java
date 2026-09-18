package demo.catalogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Public-facing view of a {@link demo.catalogservice.entities.Show}.
 * <p>
 * {@code movieId}/{@code movieTitle} and {@code screenId}/{@code screenName}
 * are flattened from the parent {@code Movie}/{@code Screen} (ModelMapper
 * resolves these automatically). {@code theatreId}/{@code theatreName} are
 * two hops away ({@code screen.theatre.id}/{@code screen.theatre.name}),
 * which ModelMapper does <em>not</em> flatten on its own, so those two are
 * set by hand in the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponseDto {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private Long screenId;
    private String screenName;
    private Long theatreId;
    private String theatreName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
