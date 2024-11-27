package com.example.springrestful.controller;

import ch.qos.logback.classic.Logger;
import com.example.springrestful.dto.*;
import com.example.springrestful.repository.UserRepository;
import com.example.springrestful.entity.User;
import com.example.springrestful.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public ResponseEntity<AuthResponse> verifyEmail(@RequestBody @Valid EmailVerificationRequest verificationRequest) {
        return ResponseEntity.ok(authService.verifyEmail(verificationRequest.getEmail(), verificationRequest.getVerificationCode()));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<AuthResponse> resendVerificationCode(@Valid @RequestBody EmailResendVerificationRequest request) {
        return ResponseEntity.ok(authService.resendVerificationCode(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.substring(7)); // Remove "Bearer " prefix
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader("Authorization") String refreshToken
    ) {
        // Remove "Bearer " prefix
        refreshToken = refreshToken.substring(7);
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

//    @PostMapping("/refresh-token")
//    public ResponseEntity<AuthResponse> refreshToken(
//            @RequestHeader("Authorization") String refreshToken
//    ) {
//        return ResponseEntity.ok(authService.refreshToken(refreshToken));
//    }
//
//    @PostMapping("/forgot-password")
//    public ResponseEntity<?> forgotPassword(
//            @RequestParam String email
//    ) {
//        authService.initiatePasswordReset(email);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity<?> resetPassword(
//            @RequestParam String token,
//            @RequestParam String newPassword
//    ) {
//        authService.resetPassword(token, newPassword);
//        return ResponseEntity.ok().build();
//    }
//
}