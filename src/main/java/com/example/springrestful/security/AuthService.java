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

    /**
     * Handles user registration process with email verification.
     *
     * @param request The registration request containing user details
     * @return AuthResponse containing user information and registration status
     */
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        try {
            log.info("📝 Starting registration process for email: {}", request.getEmail());

            // Check for existing email
            Optional<User> existingEmailUser = userRepository.findByEmail(request.getEmail());
            if (existingEmailUser.isPresent()) {
                User user = existingEmailUser.get();
                if (!user.getEmailVerified()) {
                    log.warn("❌ Registration attempt with unverified email: {}", request.getEmail());
                    throw new UserAuthenticationException(
                            "This email is already registered but not verified. Please check your email for the verification code or request a new one."
                    );
                }
                log.warn("❌ Registration attempt with existing email: {}", request.getEmail());
                throw new UserAuthenticationException(
                        "This email address is already registered. Please use a different email or login to your existing account."
                );
            }

            // Check for existing username
            Optional<User> existingUsernameUser = userRepository.findByUsername(request.getUsername());
            if (existingUsernameUser.isPresent()) {
                log.warn("❌ Registration attempt with existing username: {}", request.getUsername());
                throw new UserAuthenticationException(
                        "This username is already taken. Please choose a different username."
                );
            }

            // Create and save new user
            User user = UserMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmailVerified(false);

            // Generate verification code
            String plainVerificationCode = emailService.generateVerificationCode();
            String hashedVerificationCode = emailService.hashVerificationCode(plainVerificationCode);

            // Store verification code in Redis
            String verificationKey = VERIFICATION_CODE_PREFIX + request.getEmail();
            redisTemplate.opsForValue().set(
                    verificationKey,
                    hashedVerificationCode,
                    verificationCodeExpiryMinutes,
                    TimeUnit.MINUTES
            );

            user = userRepository.save(user);

            // Send verification email
            emailService.sendVerificationCode(user.getEmail(), plainVerificationCode);

            // Debug logging for verification code
            log.info("🔐 Registration successful for user: {}", user.getUsername());
            log.info("📧 Verification code sent to: {}", user.getEmail());
            log.debug("🔑 Verification Code (DEV ONLY): {}", plainVerificationCode);

            // Log registration details
            log.info("👤 New user registered: {}", user.getUsername());
            log.debug("📊 Registration details: Email={}, Username={}, VerificationCode={}",
                    user.getEmail(), user.getUsername(), plainVerificationCode);

            return AuthResponse.builder()
                    .user(UserMapper.toResponse(user))
                    .message("Registration successful! Please check your email for verification code.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during registration", e);
            throw new UserAuthenticationException(
                    "We encountered an unexpected error during registration. Please try again later."
            );
        }
    }

    /**
     * Verifies user's email with provided verification code.
     */
    @Transactional
    public AuthResponse verifyEmail(String email, String providedVerificationCode) {
        try {
            log.info("🔍 Starting email verification process for: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Verification attempted for non-existent email: {}", email);
                        return new UserAuthenticationException(
                                "We couldn't find an account with this email address."
                        );
                    });

            String verificationKey = VERIFICATION_CODE_PREFIX + email;
            String storedHashedCode = redisTemplate.opsForValue().get(verificationKey);

            if (storedHashedCode == null) {
                log.warn("⏰ Verification code expired for email: {}", email);
                throw new UserAuthenticationException(
                        "The verification code has expired. Please request a new one."
                );
            }

            log.debug("🔍 Verifying code for email: {}", email);
            log.debug("📝 Provided code: {}", providedVerificationCode);

            if (!passwordEncoder.matches(providedVerificationCode, storedHashedCode)) {
                log.warn("❌ Invalid verification code attempt for email: {}", email);
                throw new UserAuthenticationException(
                        "Invalid verification code. Please check and try again."
                );
            }

            user.setEmailVerified(true);
            userRepository.save(user);

            // Cleanup Redis
            redisTemplate.delete(verificationKey);
            redisTemplate.delete(VERIFICATION_ATTEMPTS_PREFIX + email);

            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            log.info("✅ Email verification successful for: {}", email);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toResponse(user))
                    .message("Email verified successfully! You can now log in.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Verification error for email: {}", email, e);
            throw new UserAuthenticationException(
                    "An error occurred during email verification. Please try again later."
            );
        }
    }

    /**
     * Resends verification code to user's email.
     */
    @Transactional
    public AuthResponse resendVerificationCode(String email) {
        try {
            log.info("📧 Processing verification code resend request for: {}", email);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Resend attempted for non-existent email: {}", email);
                        return new UserAuthenticationException(
                                "We couldn't find an account with this email address."
                        );
                    });

            if (user.getEmailVerified()) {
                log.warn("⚠️ Resend attempted for already verified email: {}", email);
                throw new UserAuthenticationException(
                        "This email is already verified. You can proceed to login."
                );
            }

            String attemptsKey = VERIFICATION_ATTEMPTS_PREFIX + email;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

            if (attempts == 1) {
                redisTemplate.expire(attemptsKey, resendLimitHours, TimeUnit.HOURS);
            }

            if (attempts > maxResendAttempts) {
                LocalDateTime nextAttemptTime = LocalDateTime.now().plusHours(resendLimitHours);
                log.warn("🚫 Maximum resend attempts reached for email: {}", email);
                throw new VerificationResendLimitException(
                        String.format("Maximum verification attempts reached. Please try again after %d hours.",
                                resendLimitHours),
                        nextAttemptTime
                );
            }

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

            log.info("📨 New verification code sent to: {}", email);
            log.debug("🔑 New verification code (DEV ONLY): {}", plainVerificationCode);
            log.debug("📊 Resend attempt {} of {}", attempts, maxResendAttempts);

            return AuthResponse.builder()
                    .user(UserMapper.toResponse(user))
                    .message(String.format("New verification code sent! Remaining attempts: %d",
                            maxResendAttempts - attempts))
                    .build();

        } catch (UserAuthenticationException | VerificationResendLimitException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Error resending verification code", e);
            throw new UserAuthenticationException(
                    "An error occurred while resending the verification code. Please try again later."
            );
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