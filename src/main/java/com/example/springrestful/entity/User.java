package com.example.springrestful.entity;

import com.example.springrestful.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Setter
@Getter
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
    private Set<UserRole> roles;

    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    private boolean twoFactorAuthEnabled;
    private String twoFactorAuthSecret;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Employee employee;

    // Method to add roles
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    // Method to remove roles
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }
}
