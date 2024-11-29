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
 * 4. Handles organization ownership and management
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

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

    // Organization Relationships
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Organization> ownedOrganizations = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Employee employeeProfile;

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

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Role Management Methods
     */
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    public boolean isAdmin() {
        return this.roles.contains(UserRole.ADMIN);
    }

    /**
     * Organization Management Methods
     */
    public void addOwnedOrganization(Organization organization) {
        this.ownedOrganizations.add(organization);
        organization.setOwner(this);
    }

    public void removeOwnedOrganization(Organization organization) {
        this.ownedOrganizations.remove(organization);
        organization.setOwner(null);
    }

    /**
     * Verification Management Methods
     */
    public void resetVerificationResendAttempts() {
        this.verificationResendCount = 0;
        this.lastVerificationResendAttempt = null;
    }

    public void incrementVerificationResendAttempts() {
        this.verificationResendCount = (this.verificationResendCount == null ? 0 : this.verificationResendCount) + 1;
        this.lastVerificationResendAttempt = LocalDateTime.now();
    }

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

    /**
     * Spring Security UserDetails Implementation
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