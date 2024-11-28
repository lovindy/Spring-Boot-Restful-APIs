package com.example.springrestful.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
    private final EmailQueueService emailQueueService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public String hashVerificationCode(String plainCode) {
        return passwordEncoder.encode(plainCode);
    }

    // This method now queues the email instead of sending it directly
    public void sendVerificationCode(String toEmail, String verificationCode) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipient email cannot be null or empty");
            }

            // Queue the email instead of sending it directly
            emailQueueService.queueEmail(toEmail, verificationCode);
            log.info("Verification email queued for: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to queue verification email", e);
            throw new RuntimeException("Failed to queue verification email: " + e.getMessage(), e);
        }
    }

    // Actual email sending logic moved to a separate method
    @Async
    protected void processAndSendEmail(String toEmail, String verificationCode) {
        try {
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
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email", e);
            // Consider implementing a retry mechanism here
        }
    }
}