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
 * User Entity representing a user in the system.
 *
 * This class serves multiple purposes:
 * 1. JPA Entity for database persistence
 * 2. Spring Security UserDetails implementation
 * 3. Manages user authentication and authorization details
 *
 * Key Features:
 * - User identification and authentication
 * - Role-based access control
 * - Email verification
 * - Password reset functionality
 * - Two-factor authentication support
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    // Identification and Authentication Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Role Management
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<UserRole> roles = new HashSet<>();

    // Email Verification Fields
    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    // Verification Attempt Tracking
    @Column(name = "verification_resend_count")
    private Integer verificationResendCount = 0;

    @Column(name = "last_verification_resend_attempt")
    private LocalDateTime lastVerificationResendAttempt;

    // Password Reset Fields
    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    // Two-Factor Authentication Fields
    @Column(name = "two_factor_auth_enabled")
    private boolean twoFactorAuthEnabled;

    @Column(name = "two_factor_auth_secret")
    private String twoFactorAuthSecret;

    // Relationships
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Employee> managedEmployees = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private Employee employeeProfile;

    /**
     * Role Management Methods
     */

    /**
     * Adds a role to the user's set of roles.
     * @param role The role to be added
     */
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    /**
     * Removes a role from the user's set of roles.
     * @param role The role to be removed
     */
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    /**
     * Checks if the user has an admin role.
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return this.roles.contains(UserRole.ADMIN);
    }

    /**
     * Verification Resend Management Methods
     */

    /**
     * Resets the verification resend attempts.
     * Clears the resend count and last attempt timestamp.
     */
    public void resetVerificationResendAttempts() {
        this.verificationResendCount = 0;
        this.lastVerificationResendAttempt = null;
    }

    /**
     * Increments the verification resend attempts.
     * Updates the resend count and last attempt timestamp.
     */
    public void incrementVerificationResendAttempts() {
        this.verificationResendCount = (this.verificationResendCount == null ? 0 : this.verificationResendCount) + 1;
        this.lastVerificationResendAttempt = LocalDateTime.now();
    }

    /**
     * Email Verification Code Management Methods
     */

    /**
     * Sets the email verification code.
     * @param verificationCode The verification code to set
     */
    public void setEmailVerificationCode(String verificationCode) {
        this.emailVerificationToken = verificationCode;
    }

    /**
     * Sets the email verification code expiry.
     * @param localDateTime The expiry timestamp
     */
    public void setEmailVerificationCodeExpiry(LocalDateTime localDateTime) {
        this.emailVerificationTokenExpiry = localDateTime;
    }

    /**
     * Gets the email verification code.
     * @return The verification code
     */
    public String getEmailVerificationCode() {
        return this.emailVerificationToken;
    }

    /**
     * Gets the email verification code expiry as an Instant.
     * @return The expiry timestamp or null
     */
    public Instant getEmailVerificationCodeExpiry() {
        return this.emailVerificationTokenExpiry == null ? null : this.emailVerificationTokenExpiry.toInstant(ZoneOffset.UTC);
    }

    /**
     * Checks if the email is verified.
     * @return true if email is verified, false otherwise
     */
    public boolean getEmailVerified() {
        return this.emailVerified;
    }

    /**
     * Spring Security UserDetails Interface Implementation
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}