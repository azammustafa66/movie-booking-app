package demo.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Credentials submitted to {@code POST /api/v1/user/login}.
 *
 * @param deviceType free-form client-supplied label (e.g. "WEB", "IOS") recorded on the session for auditing
 */
@Builder(toBuilder = true)
public record LoginRequestDto(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password cannot be empty")
        String password,
        String deviceType
) {
}
