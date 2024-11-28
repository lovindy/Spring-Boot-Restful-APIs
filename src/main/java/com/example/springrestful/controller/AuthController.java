package com.example.springrestful.controller;

import com.example.springrestful.dto.*;
import com.example.springrestful.exception.UserAuthenticationException;
import com.example.springrestful.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        try {
            log.debug("Processing registration request for email: {}", request.getEmail());
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Registration failed for email: {}", request.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {}", request.getEmail(), e);
            throw new UserAuthenticationException("Registration failed. Please try again later.");
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestBody @Valid EmailVerificationRequest verificationRequest
    ) {
        try {
            log.debug("Processing email verification for: {}", verificationRequest.getEmail());
            AuthResponse response = authService.verifyEmail(
                    verificationRequest.getEmail(),
                    verificationRequest.getVerificationCode()
            );
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Email verification failed for: {}", verificationRequest.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during email verification for: {}", verificationRequest.getEmail(), e);
            throw new UserAuthenticationException("Email verification failed. Please try again later.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthResponse> resendVerificationCode(
            @Valid @RequestBody EmailResendVerificationRequest request
    ) {
        try {
            log.debug("Processing verification code resend for: {}", request.getEmail());
            AuthResponse response = authService.resendVerificationCode(request.getEmail());
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Verification code resend failed for: {}", request.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during verification code resend for: {}", request.getEmail(), e);
            throw new UserAuthenticationException("Failed to resend verification code. Please try again later.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            log.debug("Processing login request for: {}", request.getEmail());
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Login failed for: {}", request.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for: {}", request.getEmail(), e);
            throw new UserAuthenticationException("Login failed. Please try again later.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix
            String jwtToken = token.substring(7);
            AuthResponse response = authService.logout(jwtToken);
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Logout failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            throw new UserAuthenticationException("Logout failed. Please try again later.");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken
    ) {
        try {
            // Remove "Bearer " prefix
            String token = refreshToken.substring(7);
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (UserAuthenticationException e) {
            log.warn("Token refresh failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            throw new UserAuthenticationException("Token refresh failed. Please try again later.");
        }
    }
}