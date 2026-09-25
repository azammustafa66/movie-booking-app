package demo.userservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.userservice.TestContainerConfig;
import demo.userservice.dto.LoginRequestDto;
import demo.userservice.dto.SignUpRequestDto;
import demo.userservice.entities.AppUser;
import demo.userservice.repos.UserRepository;
import demo.userservice.repos.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Integration tests for {@link UserController}: real Spring context, real database,
 * requests driven through {@link MockMvc} rather than calling the service directly —
 * so these also exercise {@code GlobalExceptionHandler} and {@code @Valid} validation,
 * not just the controller method itself.
 * <p>
 * The database is a throwaway Postgres Testcontainer ({@link TestContainerConfig}), started
 * fresh for this test run — requires Docker to be running locally, but no manually-managed
 * database (unlike {@code UserWorkflowIntegrationTest}, which still targets a real, long-lived
 * Postgres via application.yaml's default datasource).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String RAW_PASSWORD = "secure123";

    @BeforeEach
    void setUp() {
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class Signup {

        @Test
        void whenValidRequest_returns201WithCreatedUser() throws Exception {
            // assign
            SignUpRequestDto request = new SignUpRequestDto("Jane", "Doe", "jane@example.com", "secure123");

            // act + assert
            mockMvc.perform(post("/api/v1/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("jane@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Jane"));
        }

        @Test
        void whenEmailAlreadyRegistered_returns409() throws Exception {
            SignUpRequestDto request = new SignUpRequestDto("Jane", "Doe", "jane@example.com", "secure123");

            mockMvc.perform(post("/api/v1/user/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(
                            post("/api/v1/user/signup")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void whenPasswordTooShort_returns400WithFieldError() throws Exception {
            SignUpRequestDto request = new SignUpRequestDto("Jane", "Doe", "jane@example.com", "pass");

            mockMvc.perform(
                            post("/api/v1/user/signup")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.password").value("Password must be at least 8 characters and max 32 characters")
                    );
        }
    }

    @Nested
    class Login {

        private AppUser existingUser;
        private LoginRequestDto validLoginRequest;

        @BeforeEach
        void seedExistingUser() {
            existingUser = new AppUser();
            existingUser.setFirstName("Jane");
            existingUser.setLastName("Doe");
            existingUser.setEmail("jane@example.com");
            existingUser.setPassword(passwordEncoder.encode(RAW_PASSWORD));

            userRepository.save(existingUser);

            validLoginRequest = LoginRequestDto.builder()
                    .email(existingUser.getEmail())
                    .password(RAW_PASSWORD)
                    .deviceType("Web")
                    .build();
        }

        @Test
        void whenCorrectEmailAndPassword_returns200WithTokens() throws Exception {
            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            assertThat(userSessionRepository.findAll()).hasSize(1);
        }

        // TODO: whenWrongPassword_returns401
        // Same request but with a wrong password string. Assert status().isUnauthorized().
        // This is what proves GlobalExceptionHandler.handleBadCredentialsException wires up.
        @Test
        void whenWrongPassword_returns401() throws Exception {
            LoginRequestDto wrongPasswordRequest = validLoginRequest.toBuilder()
                    .password("wrongPassword")
                    .build();
            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // TODO: whenUnknownEmail_returns401
        // A LoginRequestDto for an email never saved via seedExistingUser(). Same 401 expectation
        // as above — both "wrong password" and "unknown email" go through the same BadCredentialsException.
        @Test
        void whenUnknownEmail_returns401() throws Exception {
            userRepository.deleteAll();
            mockMvc.perform(post("/api/v1/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // TODO: whenBlankEmail_returns400WithFieldError
        // LoginRequestDto("", RAW_PASSWORD, "Mobile") — @NotBlank on LoginRequestDto.email.
        // Same @Valid -> 400 pattern as the Signup validation test.

    }
}
