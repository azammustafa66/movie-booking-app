package demo.catalogservice.dto;

import demo.catalogservice.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Public-facing view of a {@link demo.catalogservice.entities.Seat}.
 * <p>
 * {@code screenId} is flattened from the parent {@code Screen} (ModelMapper
 * resolves this from {@code screen.id} automatically) so the response never
 * nests the full {@code Screen} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponseDto {
    private Long id;
    private String rowLabel;
    private Integer seatNumber;
    private SeatType seatType;
    private Long screenId;
}
