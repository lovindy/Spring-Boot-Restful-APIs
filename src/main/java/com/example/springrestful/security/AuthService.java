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
import java.time.ZoneId;
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
        // Validation checks
        Optional<User> existingEmailUser = userRepository.findByEmail(request.getEmail());
        Optional<User> existingUsernameUser = userRepository.findByUsername(request.getUsername());

        if (existingEmailUser.isPresent()) {
            throw new UserAuthenticationException("Email is already registered");
        }

        if (existingUsernameUser.isPresent()) {
            throw new UserAuthenticationException("Username already exists");
        }

        // Create new user
        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Generate and store verification code in Redis
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

        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse verifyEmail(String email, String providedVerificationCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthenticationException("User not found"));

        String verificationKey = VERIFICATION_CODE_PREFIX + email;
        String storedHashedCode = redisTemplate.opsForValue().get(verificationKey);

        if (storedHashedCode == null) {
            throw new UserAuthenticationException("Verification code has expired");
        }

        if (!passwordEncoder.matches(providedVerificationCode, storedHashedCode)) {
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

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse resendVerificationCode(String email) {
        log.info("Attempting to resend verification code for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserAuthenticationException("User not found"));

        if (user.getEmailVerified()) {
            throw new UserAuthenticationException("Email is already verified");
        }

        String attemptsKey = VERIFICATION_ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, resendLimitHours, TimeUnit.HOURS);
        }

        if (attempts > maxResendAttempts) {
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

        log.info("Verification code resent successfully for email: {}", email);
        return AuthResponse.builder()
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            log.debug("Attempting login for email: {}", request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserAuthenticationException("No account found with this email address"));

            if (!user.getEmailVerified()) {
                throw new UserAuthenticationException("Email not verified. Please verify your email address before logging in.");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Invalidate all previous sessions for this user
            jwtUtil.invalidateAllUserSessions(userDetails.getUsername());

            // Generate new tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserMapper.toResponse(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw new UserAuthenticationException("Invalid credentials");
        } catch (Exception e) {
            log.error("Login error", e);
            throw new UserAuthenticationException("Login failed");
        }
    }

    public AuthResponse logout(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            jwtUtil.invalidateAllUserSessions(username);
            jwtUtil.invalidateToken(token);

            return AuthResponse.builder()
                    .message("Logged out successfully")
                    .build();
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw new UserAuthenticationException("Logout failed");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (jwtUtil.isTokenBlacklisted(refreshToken)) {
                throw new UserAuthenticationException("Invalid refresh token");
            }

            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken, userDetails)) {
                jwtUtil.invalidateToken(refreshToken);

                String newAccessToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

                User user = (User) userDetails;
                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(newRefreshToken)
                        .user(UserMapper.toResponse(user))
                        .build();
            } else {
                throw new UserAuthenticationException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new UserAuthenticationException("Token refresh failed");
        }
    }
}