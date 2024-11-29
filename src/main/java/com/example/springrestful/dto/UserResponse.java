package com.example.springrestful.dto;

import com.example.springrestful.entity.User;
import com.example.springrestful.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String username;
    private Set<UserRole> roles;
    private boolean emailVerified;
    private boolean twoFactorAuthEnabled;
    private int managedEmployeesCount;

    public static UserResponse fromEntity(User owner) {
        return null;
    }
}
