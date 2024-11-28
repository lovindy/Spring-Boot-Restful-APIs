package com.example.springrestful.controller;

import com.example.springrestful.dto.*;
import com.example.springrestful.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.logout(token.substring(7)));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken.substring(7)));
    }
}