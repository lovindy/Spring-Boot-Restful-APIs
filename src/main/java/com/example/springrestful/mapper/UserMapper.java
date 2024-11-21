package com.example.springrestful.mapper;

import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static User toEntity(UserRegistrationRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        return user;
    }

    public static UserRegistrationRequest toDto(User user) {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail(user.getEmail());
        request.setUsername(user.getUsername());
        return request;
    }
}