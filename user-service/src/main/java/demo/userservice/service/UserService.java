package demo.userservice.service;

import demo.userservice.dto.*;
import demo.userservice.entities.AppUser;
import demo.userservice.entities.UserSession;
import demo.userservice.exceptions.BadCredentialsException;
import demo.userservice.exceptions.UserAlreadyExistsException;
import demo.userservice.repos.UserRepository;
import demo.userservice.repos.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Core user account and session workflows: signup, login, refresh-token
 * rotation, and logout.
 * <p>
 * Refresh tokens are never stored in plaintext — only their SHA-256 hash is
 * persisted on {@link UserSession}, so a leaked database cannot be used to
 * forge sessions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration-seconds}")
    Long refreshTokenExpirationSeconds;

    /**
     * Registers a new user with a hashed password and the default role.
     *
     * @throws UserAlreadyExistsException if the email is already registered
     */
    public SignUpResponseDto createUser(SignUpRequestDto signUpRequest) {
        log.info("Creating user {}", signUpRequest.getEmail());

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with email already exists");
        }
        AppUser user = new AppUser();
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        // Role defaults to CUSTOMER on the entity itself; no need to set it here.

        return modelMapper.map(userRepository.save(user), SignUpResponseDto.class);
    }

    /**
     * Authenticates a user by email/password and opens a new session, returning
     * a fresh access/refresh token pair. Request metadata (IP, user agent,
     * device type) is recorded on the session for auditing and future revocation.
     *
     * @throws BadCredentialsException if the email is unknown or the password doesn't match
     */
    public LoginResponseDto loginUser(LoginRequestDto loginRequest, HttpServletRequest request) {
        log.info("Login attempt for: {}", loginRequest.email());

        AppUser user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken();
        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenHash(hashRefreshToken(refreshToken));
        session.setExpiresAt(now.plusSeconds(refreshTokenExpirationSeconds));
        session.setLastUsedAt(now);
        session.setDeviceType(loginRequest.deviceType());
        session.setIpAddress(request.getRemoteAddr());
        session.setUserAgent(request.getHeader("User-Agent"));
        userSessionRepository.save(session);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    /**
     * Rotates a refresh token: validates the presented token, revokes it, and
     * issues a brand-new access/refresh token pair on a new session. Rotation
     * (rather than reuse) limits the blast radius if a refresh token is stolen,
     * since a stolen token can only be replayed once before it stops working.
     *
     * @throws BadCredentialsException if the token is unknown, revoked, or expired
     */
    @Transactional
    public LoginResponseDto refreshAccessToken(RefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        String refreshToken = request.refreshToken();
        String tokenHash = hashRefreshToken(refreshToken);

        UserSession oldSession = userSessionRepository
                .findByRefreshTokenHash(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token")
                );

        LocalDateTime now = LocalDateTime.now();

        if (oldSession.isRevoked() || oldSession.getExpiresAt().isBefore(now)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        AppUser user = oldSession.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.generateRefreshToken();

        // Revoke old refresh token
        oldSession.setRevoked(true);
        oldSession.setLastUsedAt(now);

        userSessionRepository.save(oldSession);

        // Create new session
        UserSession newSession = new UserSession();
        newSession.setUser(user);
        newSession.setRefreshTokenHash(hashRefreshToken(newRefreshToken));
        newSession.setExpiresAt(now.plusSeconds(refreshTokenExpirationSeconds));
        newSession.setLastUsedAt(now);

        // Preserve/update metadata
        newSession.setDeviceType(oldSession.getDeviceType());
        newSession.setIpAddress(httpRequest.getRemoteAddr());
        newSession.setUserAgent(httpRequest.getHeader("User-Agent"));

        userSessionRepository.save(newSession);

        return new LoginResponseDto(
                newAccessToken,
                newRefreshToken
        );
    }

    /**
     * Revokes the single session identified by the given refresh token
     * (i.e. logs out the current device only).
     *
     * @throws BadCredentialsException if the token is unknown
     */
    @Transactional
    public void logout(RefreshTokenRequestDto request) {
        String tokenHash = hashRefreshToken(request.refreshToken());
        UserSession session = userSessionRepository
                .findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        session.setRevoked(true);
        userSessionRepository.save(session);
    }

    /**
     * Revokes every active session belonging to the user identified by the
     * given refresh token (i.e. logs out all devices).
     *
     * @throws BadCredentialsException if the token is unknown
     */
    @Transactional
    public void logoutAll(RefreshTokenRequestDto request) {
        String tokenHash = hashRefreshToken(request.refreshToken());

        UserSession currentSession = userSessionRepository
                .findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        AppUser user = currentSession.getUser();

        userSessionRepository.revokeAllByUser(user);
    }

    /**
     * Hashes a refresh token with SHA-256 so the raw token value never has to
     * be stored at rest. Refresh tokens are already high-entropy random values
     * (see {@link RefreshTokenService}), so an unsalted fast hash is sufficient
     * here — this is a lookup key, not a password hash.
     */
    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedRefreshToken = md.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedRefreshToken);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
