package demo.catalogservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Body for admin/vendor create/update of a
 * {@link demo.catalogservice.entities.Show}. Shared by both roles; a
 * vendor's {@code screenId} must resolve to a screen under one of their own
 * theatres (enforced in {@code VendorCatalogService}, not by this DTO).
 * {@code movieId} isn't ownership-restricted — any existing catalog movie
 * can be scheduled.
 */
@Getter
@Setter
public class ShowRequestDto {

    @NotNull(message = "Movie ID is required")
    @Positive(message = "Movie ID cannot be less than 0")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    @Positive(message = "Screen ID cannot be less than 0")
    private Long screenId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}
