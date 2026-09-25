package demo.userservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the {@code jakarta.validation} constraints on {@link SignUpRequestDto} directly,
 * bypassing Spring/HTTP entirely — cheap insurance against an annotation being missing or
 * misconfigured on a field, independent of the {@code @Valid} -> 400 pipeline already proven
 * end-to-end in {@code UserControllerIntegrationTest.Signup}.
 */
class SignUpRequestDtoValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private SignUpRequestDto validRequest() {
        return SignUpRequestDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .password("secure123")
                .build();
    }

    @Test
    void validRequest_hasNoViolations() {
        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void blankFirstName_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().firstName("").build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("First name cannot be empty");
    }

    @Test
    void nullFirstName_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().firstName(null).build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("First name cannot be empty");
    }

    @Test
    void blankLastName_hasNoViolation() {
        // lastName carries no constraints — this pins that down explicitly rather than leaving it implicit
        SignUpRequestDto request = validRequest().toBuilder().lastName("").build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankEmail_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().email("").build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("Email cannot be blank");
    }

    @Test
    void malformedEmail_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().email("not-an-email").build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Provide a valid e-mail address");
    }

    @Test
    void blankPassword_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().password("").build();

        // blank fails both @NotBlank and @Size(min = 8) at once
        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder(
                        "Password must not be empty",
                        "Password must be at least 8 characters and max 32 characters"
                );
    }

    @Test
    void passwordTooShort_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().password("short").build(); // 5 chars

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must be at least 8 characters and max 32 characters");
    }

    @Test
    void passwordTooLong_hasViolation() {
        SignUpRequestDto request = validRequest().toBuilder().password("a".repeat(33)).build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Password must be at least 8 characters and max 32 characters");
    }

    @Test
    void passwordAtMinBoundary_hasNoViolation() {
        SignUpRequestDto request = validRequest().toBuilder().password("a".repeat(8)).build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void passwordAtMaxBoundary_hasNoViolation() {
        SignUpRequestDto request = validRequest().toBuilder().password("a".repeat(32)).build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @Test
    void completelyEmptyRequest_hasViolationForEveryRequiredField() {
        SignUpRequestDto request = SignUpRequestDto.builder()
                .firstName("")
                .lastName("")
                .email("")
                .password("")
                .build();

        Set<ConstraintViolation<SignUpRequestDto>> violations = VALIDATOR.validate(request);

        // firstName, email (@NotBlank), password (@NotBlank + @Size) = 4 violations; lastName is unconstrained
        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("firstName", "email", "password", "password");
    }
}
