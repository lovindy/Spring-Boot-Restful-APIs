package com.example.springrestful.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {
    private static final String EMAIL_QUEUE_KEY = "email:queue";
    private static final String EMAIL_PROCESSING_KEY = "email:processing";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void queueEmail(String toEmail, String verificationCode) {
        try {
            Map<String, String> emailData = Map.of(
                    "toEmail", toEmail,
                    "verificationCode", verificationCode
            );
            String emailJson = objectMapper.writeValueAsString(emailData);
            redisTemplate.opsForList().rightPush(EMAIL_QUEUE_KEY, emailJson);
            log.debug("Email queued successfully for: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to queue email", e);
            throw new RuntimeException("Failed to queue email", e);
        }
    }

    public Map<String, String> dequeueEmail() {
        try {
            String emailJson = redisTemplate.opsForList().leftPop(EMAIL_QUEUE_KEY);
            if (emailJson != null) {
                return objectMapper.readValue(emailJson, Map.class);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to dequeue email", e);
            return null;
        }
    }
}