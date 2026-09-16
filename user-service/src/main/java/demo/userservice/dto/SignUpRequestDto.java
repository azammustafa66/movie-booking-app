package demo.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Registration payload submitted to {@code POST /api/v1/user/signup}. */
@Data
public class SignUpRequestDto {

    @NotBlank(message = "First name cannot be empty")
    private String firstName;
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Provide a valid e-mail address")
    private String email;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, max = 32, message = "Password must be at least 8 characters and max 32 characters")
    private String password;
}
