package demo.userservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the {@code jakarta.validation} constraints on {@link LoginRequestDto} directly,
 * bypassing Spring/HTTP entirely.
 */
class LoginRequestDtoValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private LoginRequestDto validRequest() {
        return LoginRequestDto.builder()
                .email("jane@example.com")
                .password("secure123")
                .deviceType("Web")
                .build();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(VALIDATOR.validate(validRequest())).isEmpty();
    }

    @Test
    void blankDeviceType_hasNoViolation() {
        // deviceType carries no constraints — pinned down explicitly rather than left implicit
        LoginRequestDto request = validRequest().toBuilder().deviceType("").build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void nullDeviceType_hasNoViolation() {
        LoginRequestDto request = validRequest().toBuilder().deviceType(null).build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void blankEmail_hasViolation() {
        LoginRequestDto request = validRequest().toBuilder().email("").build();

        Set<ConstraintViolation<LoginRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be blank");
    }

    @Test
    void malformedEmail_hasViolation() {
        LoginRequestDto request = validRequest().toBuilder().email("not-an-email").build();

        Set<ConstraintViolation<LoginRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
    }

    @Test
    void blankPassword_hasViolation() {
        LoginRequestDto request = validRequest().toBuilder().password("").build();

        Set<ConstraintViolation<LoginRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password cannot be empty");
    }

    @Test
    void completelyEmptyRequest_hasViolationForEveryRequiredField() {
        LoginRequestDto request = LoginRequestDto.builder()
                .email("")
                .password("")
                .deviceType("")
                .build();

        Set<ConstraintViolation<LoginRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "password");
    }
}
