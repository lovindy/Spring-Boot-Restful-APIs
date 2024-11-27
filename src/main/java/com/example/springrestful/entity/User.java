package com.example.springrestful.entity;

import com.example.springrestful.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity class representing a User in the system.
 * Implements UserDetails for Spring Security compatibility.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    // Unique identifier for each user
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User's unique email (used for authentication)
    @Column(unique = true, nullable = false)
    private String email;

    // Unique username for the user
    @Column(unique = true, nullable = false)
    private String username;

    // Encrypted password
    @Column(nullable = false)
    private String password;

    // Roles assigned to the user (e.g., ADMIN, USER)
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    // Email verification details
    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    // Password reset details
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    // Two-factor authentication details
    private boolean twoFactorAuthEnabled;
    private String twoFactorAuthSecret;

    // Association: Users managing employees
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Employee> managedEmployees = new HashSet<>();

    // Association: User's employee profile (if any)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Employee employeeProfile;

    /**
     * Add a role to the user's roles set.
     * @param role Role to be added.
     */
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    /**
     * Remove a role from the user's roles set.
     * @param role Role to be removed.
     */
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    /**
     * Check if the user is an admin.
     * @return true if the user has ADMIN role, false otherwise.
     */
    public boolean isAdmin() {
        return this.roles.contains(UserRole.ADMIN);
    }

    // UserDetails interface methods implementation

    /**
     * Get user authorities based on roles.
     * Each role is prefixed with "ROLE_" as required by Spring Security.
     * @return Set of GrantedAuthority objects.
     */
    @Override
    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role.name())
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Account is never considered expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Account is never considered locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Credentials are never considered expired
    }

    @Override
    public boolean isEnabled() {
        return true;  // Account is always enabled
    }

    // Added methods to resolve the compilation errors
    public void setEmailVerificationCode(String verificationCode) {
        this.emailVerificationToken = verificationCode;
    }

    public void setEmailVerificationCodeExpiry(LocalDateTime localDateTime) {
        this.emailVerificationTokenExpiry = localDateTime;
    }

    public String getEmailVerificationCode() {
        return this.emailVerificationToken;
    }

    public Instant getEmailVerificationCodeExpiry() {
        return this.emailVerificationTokenExpiry == null ? null : this.emailVerificationTokenExpiry.toInstant(ZoneOffset.UTC);
    }

    public boolean getEmailVerified() {
        return this.emailVerified;
    }

    @Column(name = "verification_resend_count")
    private Integer verificationResendCount = 0;

    @Column(name = "last_verification_resend_attempt")
    private LocalDateTime lastVerificationResendAttempt;

    // Method to reset resend count
    public void resetVerificationResendAttempts() {
        this.verificationResendCount = 0;
        this.lastVerificationResendAttempt = null;
    }

    // Method to increment resend count
    public void incrementVerificationResendAttempts() {
        this.verificationResendCount = (this.verificationResendCount == null ? 0 : this.verificationResendCount) + 1;
        this.lastVerificationResendAttempt = LocalDateTime.now();
    }
}
