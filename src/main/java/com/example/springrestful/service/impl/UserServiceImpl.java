package com.example.springrestful.service.impl;

import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;
import com.example.springrestful.repository.UserRepository;
import com.example.springrestful.service.UserService;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public User registerUser(UserRegistrationRequest request) {

        // Check if user exists
        if (userRepository.findByUsername(request.getUsername)) {

            boolean user = userRepository.findByUsername(request.getUsername);
        }

        return null;
    }

}
