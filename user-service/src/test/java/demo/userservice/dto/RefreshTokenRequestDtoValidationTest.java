package demo.userservice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the {@code jakarta.validation} constraints on {@link RefreshTokenRequestDto}
 * directly, bypassing Spring/HTTP entirely.
 */
class RefreshTokenRequestDtoValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequest_hasNoViolations() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("some-refresh-token")
                .build();

        assertThat(VALIDATOR.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void blankOrNullRefreshToken_hasViolation(String invalidRefreshToken) {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken(invalidRefreshToken)
                .build();

        Set<ConstraintViolation<RefreshTokenRequestDto>> violations = VALIDATOR.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Refresh token cannot be empty");
    }
}
