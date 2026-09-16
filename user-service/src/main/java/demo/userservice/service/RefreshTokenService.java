package demo.userservice.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates opaque, high-entropy refresh tokens. Only the SHA-256 hash of the
 * generated value is ever persisted (see {@code UserService#hashRefreshToken}),
 * so the raw token returned here must be treated as a bearer credential by callers.
 */
@Service
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final SecureRandom random = new SecureRandom();

    /**
     * @return a URL-safe, unpadded, base64-encoded random token backed by
     * {@value #TOKEN_BYTE_LENGTH} bytes of secure randomness.
     */
    public String generateRefreshToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        random.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
