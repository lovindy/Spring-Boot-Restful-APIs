package com.example.springrestful.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailProcessor {

    private final EmailQueueService emailQueueService;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 1000) // Process every second
    public void processEmailQueue() {
        try {
            Map<String, String> emailData = emailQueueService.dequeueEmail();
            if (emailData != null) {
                String toEmail = emailData.get("toEmail");
                String verificationCode = emailData.get("verificationCode");
                emailService.processAndSendEmail(toEmail, verificationCode);
            }
        } catch (Exception e) {
            log.error("Error processing email queue", e);
        }
    }
}