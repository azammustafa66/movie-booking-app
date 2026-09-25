package demo.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/** Refresh token payload used by {@code /refresh}, {@code /logout}, and {@code /logout-all}. */
@Builder(toBuilder = true)
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token cannot be empty")
        String refreshToken
) {
}
