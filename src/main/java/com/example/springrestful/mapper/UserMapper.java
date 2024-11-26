package com.example.springrestful.mapper;

import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.dto.UserResponse;
import com.example.springrestful.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static User toEntity(UserRegistrationRequest request) {
        return User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .build();
    }

    public static UserRegistrationRequest toDto(User user) {
        return UserRegistrationRequest.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(user.getRoles())
                .emailVerified(user.getEmailVerified())
                .twoFactorAuthEnabled(user.isTwoFactorAuthEnabled())
                .build();
    }
}