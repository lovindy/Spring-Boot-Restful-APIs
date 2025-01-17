package com.example.springrestful.repository;

import com.example.springrestful.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, Long> {

    // Automatically generate the query to find a user by their email verification token
    static Optional<User> findByEmailVerificationToken(String token) {
        return null;
    }

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);
}
