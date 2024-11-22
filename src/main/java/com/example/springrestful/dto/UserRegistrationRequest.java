package com.example.springrestful.dto;

import com.example.springrestful.enums.UserRole;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    private String email;
    private String username;
    private String password;
    private UserRole role;
}