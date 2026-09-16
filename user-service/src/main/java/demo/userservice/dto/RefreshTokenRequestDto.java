package demo.userservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Refresh token payload used by {@code /refresh}, {@code /logout}, and {@code /logout-all}. */
public record RefreshTokenRequestDto(
        @NotBlank(message = "Refresh token cannot be empty")
        String refreshToken
) {
}
