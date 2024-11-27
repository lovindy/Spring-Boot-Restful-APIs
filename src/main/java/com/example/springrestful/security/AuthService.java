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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    // Register request
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Check for existing users by email and username
        Optional<User> existingEmailUser = userRepository.findByEmail(request.getEmail());
        Optional<User> existingUsernameUser = userRepository.findByUsername(request.getUsername());

        // Validate unique email and username
        if (existingEmailUser.isPresent()) {
            throw new UserAuthenticationException("Email is already registered");
        }

        if (existingUsernameUser.isPresent()) {
            throw new UserAuthenticationException("Username already exists");
        }

        // Create a new user
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Generate verification code
        String plainVerificationCode = emailService.generateVerificationCode();
        String hashedVerificationCode = emailService.hashVerificationCode(plainVerificationCode);

        // Set verification details
        user.setEmailVerificationCode(hashedVerificationCode);
        user.setEmailVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        user.setEmailVerified(false);

        // Save the new user
        user = userRepository.save(user);

        // Send plain verification code via email
        emailService.sendVerificationCode(user.getEmail(), plainVerificationCode);

        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse verifyEmail(String email, String providedVerificationCode) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthenticationException("User not found"));

        // Check if code is expired
//        if (user.getEmailVerificationCodeExpiry().isBefore(Instant.from(LocalDateTime.now()))) {
//            throw new UserAuthenticationException("Verification code has expired");
//        }

        if (user.getEmailVerificationCodeExpiry()
                .isBefore(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant())) {
            throw new UserAuthenticationException("Verification code has expired");
        }


        // Verify the code using secure password matching
        if (!passwordEncoder.matches(providedVerificationCode, user.getEmailVerificationCode())) {
            throw new UserAuthenticationException("Invalid verification code");
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

    @Transactional
    public AuthResponse resendVerificationCode(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthenticationException("User not found"));

        // Check if email is already verified
        if (user.getEmailVerified()) {
            throw new UserAuthenticationException("Email is already verified");
        }

        // Generate new verification code
        String plainVerificationCode = emailService.generateVerificationCode();
        String hashedVerificationCode = emailService.hashVerificationCode(plainVerificationCode);

        // Update verification details
        user.setEmailVerificationCode(hashedVerificationCode);
        user.setEmailVerificationCodeExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send new verification code via email
        emailService.sendVerificationCode(user.getEmail(), plainVerificationCode);

        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .build();
    }

    // Login request
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

            // Return response with tokens since login successfully
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