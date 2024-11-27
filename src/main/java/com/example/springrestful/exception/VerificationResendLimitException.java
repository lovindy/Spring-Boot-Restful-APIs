package com.example.springrestful.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VerificationResendLimitException extends RuntimeException {
    private final LocalDateTime retryAfter;

    public VerificationResendLimitException(String message, LocalDateTime retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
}
