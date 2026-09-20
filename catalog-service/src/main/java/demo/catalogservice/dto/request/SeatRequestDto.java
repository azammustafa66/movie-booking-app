package demo.catalogservice.dto.request;

import demo.catalogservice.enums.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Body for admin/vendor create/update of a
 * {@link demo.catalogservice.entities.Seat}. Shared by both roles; a
 * vendor's {@code screenId} must resolve to a screen under one of their own
 * theatres (enforced in {@code VendorCatalogService}, not by this DTO).
 */
@Getter
@Setter
public class SeatRequestDto {

    @NotBlank(message = "Row label cannot be blank")
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    @Positive(message = "Seat number must be greater than 0")
    private Integer seatNumber;

    @NotNull(message = "Seat type is required")
    private SeatType seatType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Screen ID is required")
    @Positive(message = "Screen ID cannot be less than 0")
    private Long screenId;
}
