package com.example.springrestful.dto;

import lombok.Data;

@Data
public class UserSignInRequest {

    private String username;
    private String password;
}
