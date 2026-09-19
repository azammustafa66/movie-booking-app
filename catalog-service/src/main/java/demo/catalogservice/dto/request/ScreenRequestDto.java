package demo.catalogservice.dto.request;

import demo.catalogservice.enums.ScreenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Body for admin/vendor create/update of a
 * {@link demo.catalogservice.entities.Screen}. Shared by both roles: the
 * shape is identical, only the allowed {@code theatreId} differs — an admin
 * may target any theatre, a vendor only one of their own (enforced in
 * {@code VendorCatalogService}, not by this DTO).
 */
@Getter
@Setter
public class ScreenRequestDto {

    @NotBlank(message = "Screen name cannot be blank")
    private String name;

    @NotNull(message = "Theatre ID is required")
    @Positive(message = "Theatre ID cannot be less than 0")
    private Long theatreId;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "Screen type is required")
    private ScreenType screenType;
}
