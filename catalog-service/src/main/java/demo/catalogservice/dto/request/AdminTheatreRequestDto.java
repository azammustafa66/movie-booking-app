package demo.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for admin create/update of a {@link demo.catalogservice.entities.Theatre}.
 * <p>
 * Unlike {@link VendorTheatreRequestDto}, an admin must name the owning
 * {@code vendorId} explicitly — admin isn't scoped to "my theatres", so there's
 * no caller identity to default it from.
 */
@Getter
@Setter
public class AdminTheatreRequestDto {

    @NotBlank(message = "Theatre name cannot be blank")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "State cannot be blank")
    private String state;

    @NotBlank(message = "Pincode cannot be blank")
    private String pincode;

    @NotNull(message = "Vendor ID is required")
    @Positive(message = "Vendor ID cannot be less than 0")
    private Long vendorId;
}
