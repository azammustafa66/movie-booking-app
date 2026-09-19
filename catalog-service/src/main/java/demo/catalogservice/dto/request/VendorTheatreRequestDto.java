package demo.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for a vendor's own create/update of a
 * {@link demo.catalogservice.entities.Theatre}.
 * <p>
 * Deliberately has no {@code vendorId} field: ownership is always the
 * calling vendor, taken from their JWT rather than trusted from the request
 * body, so a vendor can never create or reassign a theatre to someone else.
 */
@Getter
@Setter
public class VendorTheatreRequestDto {

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
}
