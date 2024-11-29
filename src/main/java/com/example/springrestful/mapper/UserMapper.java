package com.example.springrestful.mapper;

import com.example.springrestful.dto.UserDto;
import com.example.springrestful.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Map User entity to UserDto
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    // Map UserDto to User entity
    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        User user = new User();
        user.setId(userDto.getId());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        return user;
    }
}
