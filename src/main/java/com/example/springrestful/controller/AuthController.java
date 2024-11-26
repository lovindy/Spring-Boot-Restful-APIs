package com.example.springrestful.controller;

import com.example.springrestful.dto.AuthResponse;
import com.example.springrestful.dto.LoginRequest;
import com.example.springrestful.dto.UserRegistrationRequest;
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
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid UserRegistrationRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestParam String email,
            @RequestParam String verificationCode
    ) {
        return ResponseEntity.ok(authService.verifyEmail(email, verificationCode));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
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
//    @GetMapping("/verify-email")
//    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
//        Optional<User> userOptional = UserRepository.findByEmailVerificationToken(token); // Use the instance here
//
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//
//            // Check if the token has expired
//            if (user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification link has expired.");
//            }
//
//            // Set the email as verified and clear the verification token
//            user.setEmailVerified(true);
//            user.setEmailVerificationToken(null); // Clear the token after successful verification
//            user.setEmailVerificationTokenExpiry(null); // Clear the expiry time
//
//            userRepository.save(user); // Save the updated user
//
//            return ResponseEntity.ok("Email successfully verified.");
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid verification token.");
//        }
//    }

}