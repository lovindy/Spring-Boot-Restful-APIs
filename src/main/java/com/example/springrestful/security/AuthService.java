package com.example.springrestful.security;

import com.example.springrestful.dto.AuthResponse;
import com.example.springrestful.dto.LoginRequest;
import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;
import com.example.springrestful.exception.UserAuthenticationException;
import com.example.springrestful.exception.VerificationResendLimitException;
import com.example.springrestful.mapper.UserMapper;
import com.example.springrestful.repository.UserRepository;
import com.example.springrestful.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    @Value("${spring.mail.max.resend.attempts}")
    private int maxResendAttempts;

    @Value("${spring.mail.resend.limit.hours}")
    private long resendLimitHours;

    @Value("${verification.code.expiry.minutes}")
    private long verificationCodeExpiryMinutes;

    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "verification:";
    private static final String VERIFICATION_ATTEMPTS_PREFIX = "verification_attempts:";

    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        try {
            log.debug("Processing registration request for email: {}", request.getEmail());

            // Validation checks
            Optional<User> existingEmailUser = userRepository.findByEmail(request.getEmail());
            Optional<User> existingUsernameUser = userRepository.findByUsername(request.getUsername());

            if (existingEmailUser.isPresent()) {
                log.warn("Registration failed: Email already registered - {}", request.getEmail());
                throw new UserAuthenticationException("Email is already registered");
            }

            if (existingUsernameUser.isPresent()) {
                log.warn("Registration failed: Username already exists - {}", request.getUsername());
                throw new UserAuthenticationException("Username already exists");
            }

            // Create new user
            User user = UserMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            // Generate and store verification code
            String plainVerificationCode = emailService.generateVerificationCode();
            String hashedVerificationCode = emailService.hashVerificationCode(plainVerificationCode);

            String verificationKey = VERIFICATION_CODE_PREFIX + request.getEmail();
            redisTemplate.opsForValue().set(
                    verificationKey,
                    hashedVerificationCode,
                    verificationCodeExpiryMinutes,
                    TimeUnit.MINUTES
            );

            user.setEmailVerified(false);
            user = userRepository.save(user);

            // Send verification email
            emailService.sendVerificationCode(user.getEmail(), plainVerificationCode);

            log.info("Registration successful for email: {}", request.getEmail());
            return AuthResponse.builder()
                    .user(UserMapper.toResponse(user))
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {}", request.getEmail(), e);
            throw new UserAuthenticationException("Registration failed. Please try again later.");
        }
    }

    @Transactional
    public AuthResponse verifyEmail(String email, String providedVerificationCode) {
        try {
            log.debug("Processing email verification for: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Email verification failed: User not found - {}", email);
                        return new UserAuthenticationException("User not found");
                    });

            String verificationKey = VERIFICATION_CODE_PREFIX + email;
            String storedHashedCode = redisTemplate.opsForValue().get(verificationKey);

            if (storedHashedCode == null) {
                log.warn("Email verification failed: Code expired - {}", email);
                throw new UserAuthenticationException("Verification code has expired");
            }

            if (!passwordEncoder.matches(providedVerificationCode, storedHashedCode)) {
                log.warn("Email verification failed: Invalid code - {}", email);
                throw new UserAuthenticationException("Invalid verification code");
            }

            // Mark email as verified and clean up Redis
            user.setEmailVerified(true);
            userRepository.save(user);
            redisTemplate.delete(verificationKey);
            redisTemplate.delete(VERIFICATION_ATTEMPTS_PREFIX + email);

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            log.info("Email verification successful for: {}", email);
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toResponse(user))
                    .build();
        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during email verification for: {}", email, e);
            throw new UserAuthenticationException("Email verification failed. Please try again later.");
        }
    }

    @Transactional
    public AuthResponse resendVerificationCode(String email) {
        try {
            log.debug("Processing verification code resend for: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Verification code resend failed: User not found - {}", email);
                        return new UserAuthenticationException("User not found");
                    });

            if (user.getEmailVerified()) {
                log.warn("Verification code resend failed: Email already verified - {}", email);
                throw new UserAuthenticationException("Email is already verified");
            }

            String attemptsKey = VERIFICATION_ATTEMPTS_PREFIX + email;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

            if (attempts == 1) {
                redisTemplate.expire(attemptsKey, resendLimitHours, TimeUnit.HOURS);
            }

            if (attempts > maxResendAttempts) {
                log.warn("Verification code resend failed: Maximum attempts reached - {}", email);
                throw new VerificationResendLimitException(
                        "Maximum verification code resend attempts reached. Please try again later.",
                        LocalDateTime.now().plusHours(resendLimitHours)
                );
            }

            // Generate and store new verification code
            String plainVerificationCode = emailService.generateVerificationCode();
            String hashedVerificationCode = emailService.hashVerificationCode(plainVerificationCode);

            String verificationKey = VERIFICATION_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(
                    verificationKey,
                    hashedVerificationCode,
                    verificationCodeExpiryMinutes,
                    TimeUnit.MINUTES
            );

            emailService.sendVerificationCode(email, plainVerificationCode);

            log.info("Verification code resent successfully for: {}", email);
            return AuthResponse.builder()
                    .user(UserMapper.toResponse(user))
                    .build();
        } catch (UserAuthenticationException | VerificationResendLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during verification code resend for: {}", email, e);
            throw new UserAuthenticationException("Failed to resend verification code. Please try again later.");
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            log.debug("Processing login request for: {}", request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn("Login failed: No account found for email - {}", request.getEmail());
                        return new UserAuthenticationException("No account found with this email address");
                    });

            if (!user.getEmailVerified()) {
                log.warn("Login failed: Email not verified - {}", request.getEmail());
                throw new UserAuthenticationException("Email not verified. Please verify your email address before logging in.");
            }

            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Invalidate all previous sessions
                jwtUtil.invalidateAllUserSessions(userDetails.getUsername());

                // Generate new tokens
                String accessToken = jwtUtil.generateToken(userDetails);
                String refreshToken = jwtUtil.generateRefreshToken(userDetails);

                log.info("Login successful for email: {}", request.getEmail());
                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .user(UserMapper.toResponse(user))
                        .build();

            } catch (BadCredentialsException e) {
                log.warn("Login failed: Invalid credentials for email - {}", request.getEmail());
                throw new UserAuthenticationException("Invalid credentials");
            }

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login error for email: {}", request.getEmail(), e);
            throw new UserAuthenticationException("Login failed. Please try again later.");
        }
    }

    public AuthResponse logout(String token) {
        try {
            log.debug("Processing logout request");

            String username = jwtUtil.extractUsername(token);
            jwtUtil.invalidateAllUserSessions(username);
            jwtUtil.invalidateToken(token);

            log.info("Logout successful for user: {}", username);
            return AuthResponse.builder()
                    .message("Logged out successfully")
                    .build();
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new UserAuthenticationException("Logout failed. Please try again later.");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            log.debug("Processing token refresh request");

            if (jwtUtil.isTokenBlacklisted(refreshToken)) {
                log.warn("Token refresh failed: Token is blacklisted");
                throw new UserAuthenticationException("Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                jwtUtil.invalidateToken(refreshToken);

                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

                User user = (User) userDetails;
                log.info("Token refresh successful for user: {}", username);
                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .user(UserMapper.toResponse(user))
                        .build();
            } else {
                log.warn("Token refresh failed: Invalid token for user - {}", username);
                throw new UserAuthenticationException("Invalid refresh token");
            }
        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new UserAuthenticationException("Token refresh failed. Please try again later.");
        }
    }
}