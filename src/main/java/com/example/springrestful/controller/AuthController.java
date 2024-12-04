package com.example.springrestful.controller;

import com.example.springrestful.dto.*;
import com.example.springrestful.security.AuthService;
import com.example.springrestful.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestBody @Valid EmailVerificationRequest verificationRequest
    ) {
        return ResponseEntity.ok(authService.verifyEmail(
                verificationRequest.getEmail(),
                verificationRequest.getVerificationCode()
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthResponse> resendVerificationCode(
            @Valid @RequestBody EmailResendVerificationRequest request
    ) {
        return ResponseEntity.ok(authService.resendVerificationCode(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Extract token from request
        String token = jwtUtil.extractTokenFromRequest(request);

        // Perform logout logic
        AuthResponse authResponse = authService.logout(token, response);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Extract refresh token from request
        String refreshToken = jwtUtil.extractTokenFromRequest(request);

        // Refresh token logic
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        // Set new tokens in cookies
        jwtUtil.setAccessTokenCookie(response, authResponse.getAccessToken());
        jwtUtil.setRefreshTokenCookie(response, authResponse.getRefreshToken());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(
                request.getEmail(),
                request.getResetToken(),
                request.getNewPassword()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String token
    ) {
        String email = jwtUtil.extractUsername(token.substring(7));
        return ResponseEntity.ok(authService.changePassword(
                email,
                request.getCurrentPassword(),
                request.getNewPassword()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        try {
            AuthResponse authResponse = authService.getCurrentUserDetails();
            return ResponseEntity.ok(authResponse);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(AuthResponse.builder()
                    .message("User not authenticated")
                    .build());
        }
    }
}