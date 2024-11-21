package com.example.springrestful.service;

import com.example.springrestful.dto.UserRegistrationRequest;
import com.example.springrestful.entity.User;


public interface UserService {

    // Sign-up the super admin user
    User registerUser(UserRegistrationRequest request);

//    // Sign-in as user
//    User signInUser(UserSignInRequest request);
//
//    // Email verification
//    void verifyEmail(String token);
//
//    // Reset password using email
//    void initiatePasswordReset(String email);
//
//    // Reset password using email request
//    void resetPassword(PasswordResetRequest request);
//
//    // Reset password using current password
//    void updatePassword(PasswordUpdateRequest request);
//
//    // Two-Factor-Authentication
//    void enableTwoFactorAuth(User user);
//
//    // Verify status
//    boolean verifyTwoFactorAuth(User user, String code);
}
