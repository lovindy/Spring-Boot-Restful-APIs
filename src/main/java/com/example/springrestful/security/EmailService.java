package com.example.springrestful.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Generate a secure 6-digit verification code
     *
     * @return Plain text 6-digit verification code
     */
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Hash the verification code using BCrypt
     *
     * @param plainCode Plain text verification code
     * @return Hashed verification code
     */
    public String hashVerificationCode(String plainCode) {
        return passwordEncoder.encode(plainCode);
    }

    /**
     * Send the verification to user via the registered email
     */
    public void sendVerificationCode(String toEmail, String verificationCode) {
        try {
            // Extensive logging
            log.info("Email Sending Details:");
            log.info("From Email: {}", fromEmail);
            log.info("To Email: {}", toEmail);
            log.info("Verification Code: {}", verificationCode);

            // Additional validation
            if (toEmail == null || toEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipient email cannot be null or empty");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification Code");

            String emailContent = String.format("""
                    Hello,
                    
                    Your verification code is: %s
                    
                    This code will expire in 10 minutes.
                    
                    If you didn't request this, please ignore this email.
                    SeangDev Application Team
                    """, verificationCode);

            message.setText(emailContent);

            // Attempt to send with detailed logging
            log.info("Attempting to send email...");
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Comprehensive email sending error", e);

            // Log detailed error information
            log.error("Exception Class: {}", e.getClass().getName());
            log.error("Exception Message: {}", e.getMessage());

            // Log stack trace
            log.error("Stack Trace:", e);

            // If there's a cause, log it as well
            if (e.getCause() != null) {
                log.error("Cause Class: {}", e.getCause().getClass().getName());
                log.error("Cause Message: {}", e.getCause().getMessage());
            }

            throw new RuntimeException("Failed to send verification code email: " + e.getMessage(), e);
        }
    }
}