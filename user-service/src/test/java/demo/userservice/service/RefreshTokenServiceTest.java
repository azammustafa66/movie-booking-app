package demo.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenServiceTest {

    private static final int ITERATIONS = 1_000_000;

    private RefreshTokenService refreshTokenService;

    private String refreshToken;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService();
        refreshToken = refreshTokenService.generateRefreshToken();
    }

    @Test
    void generateRefreshToken_returns32ByteToken() {
        byte[] decoded = Base64.getUrlDecoder().decode(refreshToken);
        assertThat(decoded).hasSize(32);
    }

    @Test
    void generateRefreshToken_checkIfDoesntContainChars() {
        assertThat(refreshToken).doesNotContain("+", "/", "=");
    }

    @Test
    void generateRefreshTokens_andCheckIfAllAreUnique() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < ITERATIONS; i++) {
            tokens.add(refreshTokenService.generateRefreshToken());
        }
        assertThat(tokens).hasSize(ITERATIONS);
    }
}
