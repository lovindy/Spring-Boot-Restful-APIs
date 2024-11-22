package com.example.springrestful.entity;

import com.example.springrestful.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    private boolean twoFactorAuthEnabled;
    private String twoFactorAuthSecret;


    // For admin users - collection of employees they manage
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Employee> managedEmployees = new HashSet<>();

    // For employee users - their employee profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Employee employeeProfile;

    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    public boolean isAdmin() {
        return this.roles.contains(UserRole.ADMIN);
    }
}