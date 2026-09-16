package demo.userservice.controllers;

import demo.userservice.dto.*;
import demo.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for account signup and JWT-based authentication sessions
 * (login, refresh-token rotation, logout).
 */
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Registers a new user account. */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> createUserRequest(@Valid @RequestBody SignUpRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    /** Authenticates a user and opens a new session, returning an access/refresh token pair. */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUserRequest(@Valid @RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
        return ResponseEntity.ok(userService.loginUser(loginRequest, request));
    }

    /** Exchanges a valid, unexpired refresh token for a new access/refresh token pair. */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(userService.refreshAccessToken(request, httpRequest));
    }

    /** Revokes the session tied to the given refresh token (logs out the current device only). */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        userService.logout(request);
        return ResponseEntity.noContent().build();
    }

    /** Revokes every active session for the user tied to the given refresh token (logs out all devices). */
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@Valid @RequestBody RefreshTokenRequestDto request) {
        userService.logoutAll(request);
        return ResponseEntity.noContent().build();
    }
}
