package com.example.springrestful.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProcessor {

    private final ObjectMapper objectMapper;
    private final EmailQueueService emailQueueService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String INVITATION_QUEUE_KEY = "invitation:queue";

    @Scheduled(fixedDelay = 1000) // Process every second
    public void processEmailQueue() {
        try {
            // Process verification emails
            Map<String, String> emailData = emailQueueService.dequeueEmail();
            if (emailData != null) {
                processVerificationEmail(emailData);
            }

            // Process invitation emails
            String invitationEmail = redisTemplate.opsForList().leftPop(INVITATION_QUEUE_KEY);
            if (invitationEmail != null) {
                processInvitationEmail(invitationEmail);
            }
        } catch (Exception e) {
            log.error("💥 Error processing email queue", e);
        }
    }

    private void processVerificationEmail(Map<String, String> emailData) {
        String toEmail = emailData.get("toEmail");
        String verificationCode = emailData.get("verificationCode");
        emailService.processAndSendEmail(toEmail, verificationCode);
    }

    private void processInvitationEmail(String invitationJson) {
        try {
            Map<String, String> invitationData = objectMapper.readValue(invitationJson, Map.class);
            // Process invitation email logic
            // Implementation details...
        } catch (Exception e) {
            log.error("💥 Error processing invitation email", e);
        }
    }
}