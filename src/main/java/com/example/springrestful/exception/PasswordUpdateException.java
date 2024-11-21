package com.example.springrestful.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordUpdateException extends RuntimeException {
    public PasswordUpdateException(String message) {
        super(message);
    }
}
