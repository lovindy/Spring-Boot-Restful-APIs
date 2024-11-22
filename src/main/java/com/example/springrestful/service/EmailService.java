package com.example.springrestful.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String emailContent = String.format("""
            Hello,
            
            You have requested to reset your password. Please click the link below to reset your password:
            
            %s
            
            This link will expire in 24 hours.
            
            If you didn't request this, please ignore this email or contact support if you have concerns.
            
            Best regards,
            Your Application Team
            """, resetLink);

        message.setText(emailContent);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}