package demo.userservice;

import demo.userservice.dto.LoginRequestDto;
import demo.userservice.dto.LoginResponseDto;
import demo.userservice.dto.RefreshTokenRequestDto;
import demo.userservice.dto.SignUpRequestDto;
import demo.userservice.dto.SignUpResponseDto;
import demo.userservice.entities.AppUser;
import demo.userservice.exceptions.BadCredentialsException;
import demo.userservice.repos.UserRepository;
import demo.userservice.repos.UserSessionRepository;
import demo.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class UserWorkflowIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @BeforeEach
    void setUp() {
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testUserWorkflow() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "TestAgent");

        // 1. Sign Up
        SignUpRequestDto signUpReq = new SignUpRequestDto("Jane", "Doe", "jane@example.com", "secure123");
        SignUpResponseDto signUpResp = userService.createUser(signUpReq);
        assertThat(signUpResp.email()).isEqualTo("jane@example.com");

        AppUser savedUser = userRepository.findByEmail("jane@example.com").orElseThrow();
        assertThat(savedUser).isNotNull();

        // 2. Login
        LoginRequestDto loginReq = new LoginRequestDto("jane@example.com", "secure123", "IOS");
        LoginResponseDto loginResp = userService.loginUser(loginReq, request);
        assertThat(loginResp.accessToken()).isNotBlank();
        assertThat(loginResp.refreshToken()).isNotBlank();

        // Verify session was saved
        assertThat(userSessionRepository.findAll()).hasSize(1);

        // 3. Refresh Token
        RefreshTokenRequestDto refreshReq = new RefreshTokenRequestDto(loginResp.refreshToken());
        LoginResponseDto refreshResp = userService.refreshAccessToken(refreshReq, request);
        
        assertThat(refreshResp.accessToken()).isNotBlank();
        assertThat(refreshResp.refreshToken()).isNotBlank();
        assertThat(refreshResp.refreshToken()).isNotEqualTo(loginResp.refreshToken());

        // Verify old session is revoked, new is active
        long activeSessions = userSessionRepository.findAll().stream().filter(s -> !s.isRevoked()).count();
        long revokedSessions = userSessionRepository.findAll().stream().filter(s -> s.isRevoked()).count();
        assertThat(activeSessions).isEqualTo(1);
        assertThat(revokedSessions).isEqualTo(1);

        // 4. Logout
        RefreshTokenRequestDto logoutReq = new RefreshTokenRequestDto(refreshResp.refreshToken());
        userService.logout(logoutReq);

        // Verify all sessions are revoked
        long activeSessionsAfterLogout = userSessionRepository.findAll().stream().filter(s -> !s.isRevoked()).count();
        assertThat(activeSessionsAfterLogout).isEqualTo(0);

        // Try to refresh with logged-out token
        assertThatThrownBy(() -> userService.refreshAccessToken(logoutReq, request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid refresh token");
    }
}
