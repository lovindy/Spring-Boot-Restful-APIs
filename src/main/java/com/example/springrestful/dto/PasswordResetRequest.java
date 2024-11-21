package com.example.springrestful.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {

    private String token;
    private String newPassword;
}