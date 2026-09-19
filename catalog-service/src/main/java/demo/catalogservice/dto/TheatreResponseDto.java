package demo.catalogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Public-facing view of a {@link demo.catalogservice.entities.Theatre}.
 * <p>
 * Deliberately excludes {@code screens} — its screens are fetched separately
 * via {@code GET /api/v1/screens/theatre/{theatreId}} rather than nested here.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TheatreResponseDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Long vendorId;
}
