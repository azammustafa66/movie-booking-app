package demo.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Verifies the same tokens {@code demo.userservice.service.JwtService}
 * signs — {@code jwt.secret} here must be the exact same value as
 * user-service's own {@code jwt.secret}, since this is a shared HMAC secret,
 * not a public/private keypair. Hardcoded to {@code HmacSHA256}: JJWT's
 * {@code Keys.hmacShaKeyFor} on the signing side picks HS256 for a
 * 256–383-bit key, which is what the currently configured secret's byte
 * length resolves to — if the secret is ever regenerated at a different
 * length, JJWT could start signing with HS384/HS512 instead, and
 * verification here would start failing until this is updated to match.
 * {@code NimbusJwtDecoder.withSecretKey} intentionally doesn't auto-detect
 * the algorithm from the token itself — accepting whatever {@code alg} a
 * token claims is an algorithm-confusion vulnerability, not a convenience.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
