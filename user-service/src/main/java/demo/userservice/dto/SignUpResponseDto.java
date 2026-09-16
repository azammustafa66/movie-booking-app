package demo.userservice.dto;

import java.time.LocalDateTime;

/** Public-facing view of a newly created account. Deliberately excludes the password hash and internal id. */
public record SignUpResponseDto(
        String email,
        String firstName,
        String lastName,
        LocalDateTime createdAt
) {

}
