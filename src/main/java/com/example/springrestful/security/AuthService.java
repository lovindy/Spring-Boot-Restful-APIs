package com.example.springrestful.security;

import com.example.springrestful.dto.AuthResponse;
import com.example.springrestful.dto.LoginRequest;
import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;
import com.example.springrestful.exception.UserAuthenticationException;
import com.example.springrestful.mapper.UserMapper;
import com.example.springrestful.repository.UserRepository;
import com.example.springrestful.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAuthenticationException("Email already registered");
        }

        // Encode password
        request.setPassword(passwordEncoder.encode(request.getPassword()));

        // Create user entity
        User user = UserMapper.toEntity(request);

        // Generate verification code
        String verificationCode = emailService.generateVerificationCode();

        // Set verification code and expiry
        user.setEmailVerificationCode(verificationCode);
        user.setEmailVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        user.setEmailVerified(false);

        // Save user
        user = userRepository.save(user);

        // Send verification code via email
        emailService.sendVerificationCode(user.getEmail(), verificationCode);

        // Return response without tokens since email is not verified
        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse verifyEmail(String email, String verificationCode) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthenticationException("User not found"));

        // Check if verification code is correct and not expired
        if (!verificationCode.equals(user.getEmailVerificationCode()) ||
                user.getEmailVerificationCodeExpiry().isBefore(Instant.now())) {
            throw new UserAuthenticationException("Invalid or expired verification code");
        }

        // Mark email as verified
        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setEmailVerificationCodeExpiry(null);
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateToken((UserDetails) user);
        String refreshToken = jwtUtil.generateRefreshToken((UserDetails) user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toResponse(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            log.debug("Attempting login for email: {}", request.getEmail());

            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserAuthenticationException("User not found with email: " + request.getEmail()));

            // Check if email is verified
            if (!user.getEmailVerified()) {
                throw new UserAuthenticationException("Email not verified");
            }

            // Attempt authentication
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            log.debug("Authentication successful for email: {}", request.getEmail());

            // Generate tokens
            String accessToken = jwtUtil.generateToken((UserDetails) user);
            String refreshToken = jwtUtil.generateRefreshToken((UserDetails) user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toResponse(user))
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Authentication failed for email: {}", request.getEmail());
            throw new UserAuthenticationException("Invalid email or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {}", request.getEmail(), e);
            throw new UserAuthenticationException("An error occurred during login");
        }
    }
}