package com.example.springrestful.security;

import com.example.springrestful.entity.EmployeeInvitation;
import com.example.springrestful.exception.EmailSendingException;
import com.example.springrestful.exception.InvalidInvitationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String INVITATION_CACHE_PREFIX = "invitation:";
    private static final String INVITATION_QUEUE_KEY = "invitation:queue";
    private static final Duration INVITATION_CACHE_DURATION = Duration.ofDays(7);

    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final EmailQueueService emailQueueService;
    private final TemplateEngine templateEngine;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.invitation.base-url}")
    private String invitationBaseUrl;

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

    public void sendInvitationEmail(EmployeeInvitation invitation) {
        try {
            // Cache invitation data in Redis
            cacheInvitationData(invitation);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("organizationName", invitation.getOrganization().getName());
            variables.put("invitationLink",
                    generateInvitationLink(invitation.getInvitationToken()));
            variables.put("expiryDate", invitation.getTokenExpiry().toLocalDate().toString());
            context.setVariables(variables);

            String emailContent = templateEngine.process("invitation-email", context);

            helper.setTo(invitation.getEmail());
            helper.setSubject("Invitation to join " + invitation.getOrganization().getName());
            helper.setText(emailContent, true);
            helper.setFrom(fromEmail);

            // Queue invitation email
            queueInvitationEmail(invitation.getEmail(), emailContent);
            log.info("📨 Invitation email queued for: {}", invitation.getEmail());

        } catch (MessagingException e) {
            log.error("💥 Failed to prepare invitation email", e);
            throw new EmailSendingException("Failed to send invitation email", e);
        }
    }

    private void cacheInvitationData(EmployeeInvitation invitation) {
        try {
            String cacheKey = INVITATION_CACHE_PREFIX + invitation.getInvitationToken();
            Map<String, String> invitationData = new HashMap<>();
            invitationData.put("email", invitation.getEmail());
            invitationData.put("organizationId", invitation.getOrganization().getId().toString());
            invitationData.put("status", invitation.getStatus().toString());
            invitationData.put("expiryDate", invitation.getTokenExpiry().toString());

            redisTemplate.opsForHash().putAll(cacheKey, invitationData);
            redisTemplate.expire(cacheKey, INVITATION_CACHE_DURATION);

            log.debug("🗄️ Invitation data cached with key: {}", cacheKey);
        } catch (Exception e) {
            log.error("💥 Failed to cache invitation data", e);
            throw new InvalidInvitationException("Failed to process invitation" + e);
        }
    }

    public Optional<Map<Object, Object>> getInvitationData(String token) {
        String cacheKey = INVITATION_CACHE_PREFIX + token;
        Map<Object, Object> invitationData = redisTemplate.opsForHash().entries(cacheKey);
        return invitationData.isEmpty() ? Optional.empty() : Optional.of(invitationData);
    }

    private void queueInvitationEmail(String email, String content) {
        try {
            Map<String, String> emailData = Map.of(
                    "type", "invitation",
                    "email", email,
                    "content", content
            );
            redisTemplate.opsForList().rightPush(INVITATION_QUEUE_KEY,
                    objectMapper.writeValueAsString(emailData));
        } catch (Exception e) {
            log.error("💥 Failed to queue invitation email", e);
            throw new EmailSendingException("Failed to queue invitation email", e);
        }
    }

    private String generateInvitationLink(String token) {
        return invitationBaseUrl + "/invitations/" + token + "/accept";
    }

    private void handleEmailFailure(String toEmail, String verificationCode) {
        // Implement retry logic or dead letter queue
        log.warn("⚠️ Implementing retry logic for failed email to: {}", toEmail);
        // Add to dead letter queue or retry queue
    }
}