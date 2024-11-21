package com.example.springrestful.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {

    private String username;
    private String currentPassword;
    private String newPassword;
}
