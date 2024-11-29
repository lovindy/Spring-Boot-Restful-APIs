package com.example.springrestful.service;

import com.example.springrestful.dto.UserDto;
import com.example.springrestful.entity.User;

import java.util.List;

public interface UserService {
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    User getUserEntityById(Long userId);
}
