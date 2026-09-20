package demo.userservice.service;

import demo.userservice.entities.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues signed short-lived JWT access tokens for authenticated users.
 * <p>
 * Refresh tokens are handled separately by {@link RefreshTokenService} and are
 * opaque random strings rather than JWTs.
 */
@Service
public class JwtService {

    private static final long MILLIS_PER_SECOND = 1000L;

    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;

    /**
     * @param secret                     HMAC signing secret, injected from {@code jwt.secret}
     * @param accessTokenValiditySeconds access token lifetime in seconds, injected from
     *                                   {@code jwt.access-token-expiration-seconds}
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-seconds}") long accessTokenValiditySeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }

    /**
     * Builds a signed access token for {@code user}, embedding their id as the subject
     * and their email/role as claims.
     */
    public String generateAccessToken(AppUser user) {
        Date now = new Date();
        // accessTokenValiditySeconds is in seconds; Date arithmetic needs milliseconds.
        Date expiration = new Date(now.getTime() + accessTokenValiditySeconds * MILLIS_PER_SECOND);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }
}
