package demo.userservice.service;

import demo.userservice.dto.LoginRequestDto;
import demo.userservice.dto.LoginResponseDto;
import demo.userservice.dto.RefreshTokenRequestDto;
import demo.userservice.dto.SignUpRequestDto;
import demo.userservice.dto.SignUpResponseDto;
import demo.userservice.entities.AppUser;
import demo.userservice.entities.UserSession;
import demo.userservice.exceptions.BadCredentialsException;
import demo.userservice.exceptions.UserAlreadyExistsException;
import demo.userservice.repos.UserRepository;
import demo.userservice.repos.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserService userService;

    private static final long REFRESH_TOKEN_EXPIRES_IN_SECONDS = 604_800L;
    private static final String RAW_PASSWORD = "test123";

    private AppUser appUser;

    @BeforeEach
    void setUpUser() {
        appUser = new AppUser();
        appUser.setFirstName("TestFirstName");
        appUser.setLastName("TestLastName");
        appUser.setEmail("test@test.com");
        appUser.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        appUser.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    class CreateUser {

        private SignUpRequestDto signUpRequest;

        @BeforeEach
        void setUp() {
            signUpRequest = new SignUpRequestDto(
                    appUser.getFirstName(),
                    appUser.getLastName(),
                    appUser.getEmail(),
                    RAW_PASSWORD
            );
        }

        @Test
        void whenEmailNotRegistered_returnSuccess() {
            // assign
            when(userRepository.existsByEmail(appUser.getEmail())).thenReturn(false);
            when(userRepository.save(any(AppUser.class))).thenReturn(appUser);
            // act
            SignUpResponseDto signUpResponseDto = userService.createUser(signUpRequest);
            // verify
            assertThat(signUpResponseDto).isNotNull();
            assertThat(signUpResponseDto.email()).isEqualTo(appUser.getEmail());
            assertThat(signUpResponseDto.firstName()).isEqualTo(appUser.getFirstName());
            assertThat(signUpResponseDto.lastName()).isEqualTo(appUser.getLastName());

            ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
            verify(userRepository).save(userCaptor.capture());
            AppUser user = userCaptor.getValue();
            assertThat(user.getPassword()).isNotEqualTo(RAW_PASSWORD);
            assertThat(passwordEncoder.matches(RAW_PASSWORD, user.getPassword())).isTrue();
        }

        @Test
        void whenEmailRegistered_throwUserAlreadyExists() {
            // assign
            when(userRepository.existsByEmail(appUser.getEmail())).thenReturn(true);
            // assert
            assertThatThrownBy(() -> userService.createUser(signUpRequest))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessage("User with email already exists");
            verify(userRepository, atLeastOnce()).existsByEmail(appUser.getEmail());
            verify(userRepository, never()).save(appUser);
        }
    }

    @Nested
    class LoginUser {

        private LoginRequestDto loginRequest;
        private MockHttpServletRequest mockHttpServletRequest;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequestDto(
                    "test@test.com",
                    RAW_PASSWORD,
                    "Mobile"
            );

            mockHttpServletRequest = new MockHttpServletRequest();
            mockHttpServletRequest.setRemoteAddr("127.0.0.1");
            mockHttpServletRequest.addHeader("User-Agent", "TestAgent");

            ReflectionTestUtils.setField(userService, "refreshTokenExpirationSeconds", REFRESH_TOKEN_EXPIRES_IN_SECONDS);
        }

        @Test
        void whenCorrectEmailAndPassword_returnSuccess() {
            // assign
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(appUser));
            when(jwtService.generateAccessToken(appUser)).thenReturn("mock-access-token");
            when(refreshTokenService.generateRefreshToken()).thenReturn("mock-refresh-token");

            // act
            LoginResponseDto loginResponseDto = userService.loginUser(loginRequest, mockHttpServletRequest);

            // assert - the tokens returned come straight from our two stubbed collaborators
            assertThat(loginResponseDto.accessToken()).isEqualTo("mock-access-token");
            assertThat(loginResponseDto.refreshToken()).isEqualTo("mock-refresh-token");

            // assert - a session was actually persisted, with the right shape
            ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
            verify(userSessionRepository).save(sessionCaptor.capture());
            UserSession savedSession = sessionCaptor.getValue();

            assertThat(savedSession.getUser()).isEqualTo(appUser);
            assertThat(savedSession.isRevoked()).isFalse();
            assertThat(savedSession.getDeviceType()).isEqualTo("Mobile");
            assertThat(savedSession.getIpAddress()).isEqualTo("127.0.0.1");
            assertThat(savedSession.getUserAgent()).isEqualTo("TestAgent");
            // the raw refresh token is never stored — only its SHA-256 hash is
            assertThat(savedSession.getRefreshTokenHash()).isEqualTo(sha256Hex("mock-refresh-token"));
        }

        @Test
        void whenIncorrectEmail_throwInvalidEmailOrPassword() {
            // assign
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());
            // assert
            assertThatThrownBy(() -> userService.loginUser(loginRequest, mockHttpServletRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid email or password");
            verify(userRepository, atLeastOnce()).findByEmail(loginRequest.email());
            verify(userRepository, never()).save(any(AppUser.class));
        }

        @Test
        void whenIncorrectPassword_throwInvalidEmailOrPassword() {
            // assign
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(appUser));
            LoginRequestDto wrongPasswordRequest = new LoginRequestDto(loginRequest.email(), "wrongPassword", "TestDevice");
            assertThatThrownBy(() -> userService.loginUser(wrongPasswordRequest, mockHttpServletRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid email or password");
            verify(userSessionRepository, never()).save(any());
        }
    }

    @Nested
    class RefreshAccessToken {

        private RefreshTokenRequestDto refreshTokenRequest;
        private MockHttpServletRequest mockHttpServletRequest;
        private UserSession userSession;

        @BeforeEach
        void setUp() {
            refreshTokenRequest = new RefreshTokenRequestDto("mock-refresh-token");

            mockHttpServletRequest = new MockHttpServletRequest();
            mockHttpServletRequest.setRemoteAddr("127.0.0.1");
            mockHttpServletRequest.addHeader("User-Agent", "TestAgent");

            userSession = new UserSession();
            userSession.setUser(appUser);
            userSession.setRefreshTokenHash(sha256Hex("mock-refresh-token"));
            userSession.setExpiresAt(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRES_IN_SECONDS));
            userSession.setIpAddress(mockHttpServletRequest.getRemoteAddr());
            userSession.setDeviceType("TestDevice");
            userSession.setUserAgent(mockHttpServletRequest.getHeader("User-Agent"));


            ReflectionTestUtils.setField(userService, "refreshTokenExpirationSeconds", REFRESH_TOKEN_EXPIRES_IN_SECONDS);
        }

        @Test
        void whenValidRefreshTokenAndNotRevoked_returnNewRefreshToken() {
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(refreshTokenRequest.refreshToken()))).thenReturn(Optional.of(userSession));
            when(jwtService.generateAccessToken(appUser)).thenReturn("new-mock-access-token");
            when(refreshTokenService.generateRefreshToken()).thenReturn("new-mock-refresh-token");

            LoginResponseDto loginResponse = userService.refreshAccessToken(refreshTokenRequest, mockHttpServletRequest);
            assertThat(loginResponse.accessToken()).isEqualTo("new-mock-access-token");
            assertThat(loginResponse.refreshToken()).isEqualTo("new-mock-refresh-token");

            ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
            verify(userSessionRepository, times(2)).save(sessionCaptor.capture());
            List<UserSession> sessions = sessionCaptor.getAllValues();
            UserSession revokedSession = sessions.get(0);
            UserSession newSession = sessions.get(1);

            assertThat(revokedSession.getUser()).isEqualTo(appUser);
            assertThat(revokedSession.isRevoked()).isTrue();
            assertThat(newSession.getRefreshTokenHash()).isEqualTo(sha256Hex("new-mock-refresh-token"));
            assertThat(newSession.getIpAddress()).isEqualTo(mockHttpServletRequest.getRemoteAddr());
            assertThat(newSession.getUserAgent()).isEqualTo(mockHttpServletRequest.getHeader("User-Agent"));
            assertThat(newSession.getDeviceType()).isEqualTo("TestDevice");
        }

        @Test
        void whenInvalidRefreshToken_throwBadCredentialsException() {
            // assign
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(refreshTokenRequest.refreshToken()))).thenReturn(Optional.empty());
            // act
            assertThatThrownBy(() -> userService.refreshAccessToken(refreshTokenRequest, mockHttpServletRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid refresh token or session expired");
            verify(userSessionRepository, never()).save(any());
        }

        @Test
        void whenValidRefreshTokenButRevoked_throwBadCredentialsException() {
            userSession.setRevoked(true);
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(refreshTokenRequest.refreshToken()))).thenReturn(Optional.of(userSession));
            assertThatThrownBy(() -> userService.refreshAccessToken(refreshTokenRequest, mockHttpServletRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid refresh token or session expired");
            verify(userSessionRepository, never()).save(any());
        }
    }

    @Nested
    class Logout {

        private RefreshTokenRequestDto logoutRequest;
        private UserSession userSession;

        @BeforeEach
        void setUp() {
            logoutRequest = new RefreshTokenRequestDto("mock-refresh-token");
            userSession = new UserSession();
            userSession.setUser(appUser);
            userSession.setRefreshTokenHash(sha256Hex("mock-refresh-token"));
            userSession.setExpiresAt(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRES_IN_SECONDS));
            userSession.setIpAddress("127.0.0.1");
            userSession.setDeviceType("TestDevice");
            userSession.setUserAgent("TestAgent");
        }

        @Test
        void whenValidRefreshTokenAndNotRevoked_logoutSuccessfully() {
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(logoutRequest.refreshToken()))).thenReturn(Optional.of(userSession));

            userService.logout(logoutRequest);
            ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
            verify(userSessionRepository, atMostOnce()).save(sessionCaptor.capture());
            UserSession revokedSession = sessionCaptor.getValue();
            assertThat(revokedSession.getUser()).isEqualTo(appUser);
            assertThat(revokedSession.isRevoked()).isTrue();
            assertThat(revokedSession.getRefreshTokenHash()).isEqualTo(sha256Hex(logoutRequest.refreshToken()));
        }

        @Test
        void whenInvalidRefreshToken_throwBadCredentialsException() {
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(logoutRequest.refreshToken()))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.logout(logoutRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid refresh token or session expired");
        }

        @Test
        void whenValidRefreshTokenButRevoked_throwBadCredentialsException() {
            userSession.setRevoked(true);
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(logoutRequest.refreshToken()))).thenReturn(Optional.of(userSession));

            assertThatThrownBy(() -> userService.logout(logoutRequest)).isInstanceOf(BadCredentialsException.class).hasMessage("Invalid refresh token or session expired");
        }
    }

    @Nested
    class LogoutAll {

        private RefreshTokenRequestDto logoutAllRequest;
        private UserSession userSession;

        @BeforeEach
        void setUp() {
            logoutAllRequest = new RefreshTokenRequestDto("mock-refresh-token");
            userSession = new UserSession();
            userSession.setUser(appUser);
            userSession.setRefreshTokenHash(sha256Hex("mock-refresh-token"));
            userSession.setExpiresAt(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRES_IN_SECONDS));
            userSession.setIpAddress("127.0.0.1");
            userSession.setDeviceType("TestDevice");
            userSession.setUserAgent("TestAgent");
        }

        @Test
        void whenValidRefreshToken_revokesAllSessionsForUser() {
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(logoutAllRequest.refreshToken()))).thenReturn(Optional.of(userSession));

            userService.logoutAll(logoutAllRequest);

            verify(userSessionRepository).revokeAllByUser(appUser);
            verify(userSessionRepository, never()).save(any());
        }

        @Test
        void whenInvalidRefreshToken_throwBadCredentialsException() {
            when(userSessionRepository.findByRefreshTokenHash(sha256Hex(logoutAllRequest.refreshToken()))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.logoutAll(logoutAllRequest))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid refresh token or session expired");
            verify(userSessionRepository, never()).revokeAllByUser(any());
        }
    }

    /**
     * Mirrors UserService's private hashRefreshToken() so tests can independently verify
     * that a UserSession's stored hash matches a given raw refresh token.
     */
    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
