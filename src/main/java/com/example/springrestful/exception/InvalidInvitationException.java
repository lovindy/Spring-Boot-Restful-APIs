package com.example.springrestful.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInvitationException extends RuntimeException {
    public InvalidInvitationException(String message) {
        super(message);
    }
}