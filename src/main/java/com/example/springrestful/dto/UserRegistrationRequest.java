// com.example.spring-restful.dto.UserRegistrationRequest.java
package com.example.springrestful.dto;

import lombok.Data;

@Data
public class UserRegistrationRequest {

    public String getUsername;
    private String username;
    private String email;
    private String password;
}