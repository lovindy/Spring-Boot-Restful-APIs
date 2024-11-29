package com.example.springrestful.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EmailUtil {

    private EmailUtil() {
        // Private constructor to prevent instantiation
    }

    public static String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public static String hashVerificationCode(String plainCode, PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(plainCode);
    }

    public static SimpleMailMessage createVerificationEmail(String fromEmail, String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Email Verification Code");
        message.setText(buildVerificationEmailContent(verificationCode));
        return message;
    }

    public static Map<String, String> createEmailQueueData(String toEmail, String verificationCode) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("toEmail", toEmail);
        emailData.put("verificationCode", verificationCode);
        return emailData;
    }

    public static Map<String, String> createInvitationQueueData(String email, String content) {
        return Map.of(
                "type", "invitation",
                "email", email,
                "content", content
        );
    }

    private static String buildVerificationEmailContent(String verificationCode) {
        return String.format("""
                Hello,
                
                Your verification code is: %s
                
                This code will expire in 10 minutes.
                
                If you didn't request this, please ignore this email.
                SeangDev Application Team
                """, verificationCode);
    }

    public static void logEmailError(String message, String email, Exception e) {
        log.error("💥 {} for email: {}", message, email, e);
    }

    public static void logEmailSuccess(String message, String email) {
        log.info("📨 {} for: {}", message, email);
    }
}