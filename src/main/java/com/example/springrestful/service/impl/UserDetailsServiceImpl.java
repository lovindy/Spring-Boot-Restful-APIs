package com.example.springrestful.service.impl;

import com.example.springrestful.entity.User;
import com.example.springrestful.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use email instead of username
                user.getPassword(),
                user.getEmailVerified(), // Only allow verified users
                true, // account non-expired
                true, // credentials non-expired
                true, // account non-locked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}