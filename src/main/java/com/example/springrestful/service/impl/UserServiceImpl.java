package com.example.springrestful.service.impl;

import com.example.springrestful.entity.User;
import com.example.springrestful.exception.ResourceNotFoundException;
import com.example.springrestful.repository.AuthRepository;
import com.example.springrestful.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AuthRepository authRepository;

    @Override
    public User getUserById(Long userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
