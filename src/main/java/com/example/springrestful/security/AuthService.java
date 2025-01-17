package com.example.springrestful.security;

import com.example.springrestful.dto.AuthResponse;
import com.example.springrestful.dto.LoginRequest;
import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;
import com.example.springrestful.enums.UserRole;
import com.example.springrestful.exception.UserAuthenticationException;
import com.example.springrestful.exception.VerificationResendLimitException;
import com.example.springrestful.mapper.AuthMapper;
import com.example.springrestful.repository.AuthRepository;
import com.example.springrestful.repository.OrganizationRepository;
import com.example.springrestful.service.impl.CustomUserDetailsImpl;
import com.example.springrestful.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
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

    @Value("${jwt.password-reset-token-expiry-minutes}")
    private int passwordResetTokenExpiryMinutes;

    private final UserDetailsService userDetailsService;
    private final AuthRepository authRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "verification:";
    private static final String VERIFICATION_ATTEMPTS_PREFIX = "verification_attempts:";
    private static final String PASSWORD_RESET_TOKEN_PREFIX = "password_reset:";

    /**
     * Get the current login user id
     */
    public Long getCurrentUserId() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // Check if the principal is an instance of CustomUserDetailsImpl
        if (principal instanceof CustomUserDetailsImpl) {
            return ((CustomUserDetailsImpl) principal).getId();
        } else if (principal instanceof String) {
            // Handle cases where the principal is a String (e.g., username)
            String username = (String) principal;
            User user = authRepository.findByUsername(username)
                    .orElseThrow(() -> new AccessDeniedException("User not found"));
            return user.getId();
        } else {
            throw new AccessDeniedException("Unexpected principal type");
        }
    }

    public AuthResponse getCurrentUserDetails() throws AccessDeniedException {
        Long userId = getCurrentUserId();
        User user = authRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Map the User entity to AuthResponse
        return AuthResponse.builder()
                .user(AuthMapper.toResponse(user)) // Assuming `toResponse` converts the user entity to a DTO
                .message("User authenticated successfully")
                .build();
    }

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
            Optional<User> existingEmailUser = authRepository.findByEmail(request.getEmail());
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
            Optional<User> existingUsernameUser = authRepository.findByUsername(request.getUsername());
            if (existingUsernameUser.isPresent()) {
                log.warn("❌ Registration attempt with existing username: {}", request.getUsername());
                throw new UserAuthenticationException(
                        "This username is already taken. Please choose a different username."
                );
            }

            // Step 1: Create and save new user
            User user = AuthMapper.toEntity(request);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmailVerified(false);

            // Assign the user an ADMIN role by default
            user.setRoles(Set.of(UserRole.ADMIN));

            // Step 2: Generate and store email verification code
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

            user = authRepository.save(user);

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
                    .user(AuthMapper.toResponse(user))
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

            User user = authRepository.findByEmail(email)
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
            authRepository.save(user);

            // Cleanup Redis
            redisTemplate.delete(verificationKey);
            redisTemplate.delete(VERIFICATION_ATTEMPTS_PREFIX + email);

            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            log.info("✅ Email verification successful for: {}", email);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(AuthMapper.toResponse(user))
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

            User user = authRepository.findByEmail(email)
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
                    .user(AuthMapper.toResponse(user))
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

    /**
     * Login method
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            log.info("🔍 Starting login process for email: {}", request.getEmail());

            User user = authRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn("❌ Login failed: No account found for email: {}", request.getEmail());
                        return new UserAuthenticationException(
                                "No account found with this email address."
                        );
                    });

            if (!user.getEmailVerified()) {
                log.warn("⚠️ Login failed: Email not verified for: {}", request.getEmail());
                throw new UserAuthenticationException(
                        "Email not verified. Please verify your email address before logging in."
                );
            }

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

            // Set tokens in cookies instead of returning them in the response
            jwtUtil.generateToken(userDetails, response);
            jwtUtil.generateRefreshToken(userDetails, response);

            log.info("✅ Login successful for email: {}", request.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(AuthMapper.toResponse(user))
                    .message("Login successful!")
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("❌ Login failed: Invalid credentials for email: {}", request.getEmail());
            throw new UserAuthenticationException("Invalid credentials. Please check and try again.");
        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during login for email: {}", request.getEmail(), e);
            throw new UserAuthenticationException(
                    "We encountered an unexpected error during login. Please try again later."
            );
        }
    }

    /**
     * Logout method
     */
    public AuthResponse logout(String token, HttpServletResponse response) {
        try {
            log.info("🔒 Starting logout process.");

            // Extract current token from SecurityContextHolder or pass it as a parameter
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Invalidate sessions
            jwtUtil.invalidateAllUserSessions(username);

            // Clear authentication cookies
            jwtUtil.clearAuthenticationCookies(response);

            return AuthResponse.builder()
                    .message("Logout successful!")
                    .build();
        } catch (Exception e) {
            log.error("💥 Unexpected error during logout", e);
            throw new UserAuthenticationException(
                    "An error occurred during logout. Please try again later."
            );
        }
    }

    /**
     * Handles the refresh token process, generating new access and refresh tokens.
     *
     * @param refreshToken The refresh token provided by the client
     * @return AuthResponse containing new tokens and user information
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            log.info("🔄 Starting token refresh process.");

            // Check if the token is blacklisted
            if (jwtUtil.isTokenBlacklisted(refreshToken)) {
                log.warn("❌ Token refresh failed: Provided refresh token is blacklisted.");
                throw new UserAuthenticationException(
                        "Invalid refresh token. Please login again."
                );
            }

            // Extract the username from the token
            String username = jwtUtil.extractUsername(refreshToken);
            log.debug("🔍 Extracted username from refresh token: {}", username);

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token
            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                log.warn("❌ Token refresh failed: Invalid token for user: {}", username);
                throw new UserAuthenticationException(
                        "Invalid refresh token. Please login again."
                );
            }

            // Invalidate the old refresh token
            jwtUtil.invalidateToken(refreshToken);

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

            User user = (User) userDetails;

            log.info("✅ Token refresh successful for user: {}", username);

            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .user(AuthMapper.toResponse(user))
                    .message("Token refreshed successfully.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during token refresh.", e);
            throw new UserAuthenticationException(
                    "An error occurred during token refresh. Please try again later."
            );
        }
    }

    /**
     * Initiates the forgot password process by sending a reset token to user's email
     */
    @Transactional
    public AuthResponse forgotPassword(String email) {
        try {
            log.info("🔄 Starting password reset process for email: {}", email);

            User user = authRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Password reset attempted for non-existent email: {}", email);
                        return new UserAuthenticationException(
                                "If an account exists with this email, you will receive password reset instructions."
                        );
                    });

            if (!user.getEmailVerified()) {
                log.warn("⚠️ Password reset attempted for unverified email: {}", email);
                throw new UserAuthenticationException(
                        "Please verify your email address before resetting your password."
                );
            }

            // Generate and store reset token
            String plainResetToken = emailService.generateVerificationCode();
            String hashedResetToken = emailService.hashVerificationCode(plainResetToken);

            // Store reset token in Redis
            String resetTokenKey = PASSWORD_RESET_TOKEN_PREFIX + email;
            redisTemplate.opsForValue().set(
                    resetTokenKey,
                    hashedResetToken,
                    passwordResetTokenExpiryMinutes,
                    TimeUnit.MINUTES
            );

            // Send reset email
            emailService.sendPasswordResetToken(email, plainResetToken);

            log.info("📧 Password reset token sent to: {}", email);
            log.debug("🔑 Reset Token (DEV ONLY): {}", plainResetToken);

            return AuthResponse.builder()
                    .message("If an account exists with this email, you will receive password reset instructions.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Error in forgot password process", e);
            throw new UserAuthenticationException(
                    "An error occurred while processing your request. Please try again later."
            );
        }
    }

    /**
     * Resets user's password using the provided reset token
     */
    @Transactional
    public AuthResponse resetPassword(String email, String resetToken, String newPassword) {
        try {
            log.info("🔄 Processing password reset for email: {}", email);

            User user = authRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Password reset attempted for non-existent email: {}", email);
                        return new UserAuthenticationException("Invalid or expired reset token.");
                    });

            String resetTokenKey = PASSWORD_RESET_TOKEN_PREFIX + email;
            String storedHashedToken = redisTemplate.opsForValue().get(resetTokenKey);

            if (storedHashedToken == null) {
                log.warn("⏰ Reset token expired for email: {}", email);
                throw new UserAuthenticationException("Reset token has expired. Please request a new one.");
            }

            if (!passwordEncoder.matches(resetToken, storedHashedToken)) {
                log.warn("❌ Invalid reset token used for email: {}", email);
                throw new UserAuthenticationException("Invalid reset token.");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            authRepository.save(user);

            // Cleanup Redis and invalidate all sessions
            redisTemplate.delete(resetTokenKey);
            jwtUtil.invalidateAllUserSessions(user.getUsername());

            log.info("✅ Password reset successful for email: {}", email);

            return AuthResponse.builder()
                    .message("Password has been reset successfully. You can now login with your new password.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Error in reset password process", e);
            throw new UserAuthenticationException(
                    "An error occurred while resetting your password. Please try again later."
            );
        }
    }

    /**
     * Changes user's password (requires current password)
     */
    @Transactional
    public AuthResponse changePassword(String email, String currentPassword, String newPassword) {
        try {
            log.info("🔄 Processing password change for email: {}", email);

            User user = authRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("❌ Password change attempted for non-existent email: {}", email);
                        return new UserAuthenticationException("User not found.");
                    });

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.warn("❌ Invalid current password provided for email: {}", email);
                throw new UserAuthenticationException("Current password is incorrect.");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            authRepository.save(user);

            // Invalidate all sessions
            jwtUtil.invalidateAllUserSessions(user.getUsername());

            log.info("✅ Password change successful for email: {}", email);

            return AuthResponse.builder()
                    .message("Password changed successfully. Please login with your new password.")
                    .build();

        } catch (UserAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("💥 Error in password change process", e);
            throw new UserAuthenticationException(
                    "An error occurred while changing your password. Please try again later."
            );
        }
    }
}