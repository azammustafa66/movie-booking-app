package demo.userservice.service;

import demo.userservice.entities.AppUser;
import demo.userservice.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-1234567890-abcdefghijklmnop";
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 900L;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private AppUser appUser;
    private Claims claims;
    private String token;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(SECRET, ACCESS_TOKEN_VALIDITY_SECONDS);

        appUser = new AppUser();
        appUser.setId(42L);
        appUser.setEmail("test@test.com");
        appUser.setRole(Role.CUSTOMER);

        token = jwtService.generateAccessToken(appUser);
        claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Test
    void generateAccessToken_checkForSubjectAndClaims() {
        assertThat(claims.get("sub")).isEqualTo(appUser.getId().toString());
        assertThat(claims.get("email")).isEqualTo(appUser.getEmail());
        assertThat(claims.get("role")).isEqualTo(appUser.getRole().name());
    }

    @Test
    void generateRefreshToken_checkForExpiry() {
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime()).isEqualTo(ACCESS_TOKEN_VALIDITY_SECONDS * 1000);
    }

    @Test
    void generateRefreshTokenWithDifferentSecretKey_throwsException() {
        SecretKey invalidKey = Keys.hmacShaKeyFor("test-invalid-key-1234567890-abcdefghijklmnop".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> Jwts.parser()
                .verifyWith(invalidKey)
                .build()
                .parseSignedClaims(token)
        ).isInstanceOf(SignatureException.class);
    }
}
