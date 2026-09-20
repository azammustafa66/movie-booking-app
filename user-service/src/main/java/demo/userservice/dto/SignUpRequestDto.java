package demo.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registration payload submitted to {@code POST /api/v1/user/signup}. */
public record SignUpRequestDto(
        @NotBlank(message = "First name cannot be empty")
        String firstName,

        String lastName,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Provide a valid e-mail address")
        String email,

        @NotBlank(message = "Password must not be empty")
        @Size(min = 8, max = 32, message = "Password must be at least 8 characters and max 32 characters")
        String password
) {
}
