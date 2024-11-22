package com.example.springrestful.entity;

import com.example.springrestful.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Employee> managedEmployees = new HashSet<>();

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

    // Implementing UserDetails interface methods
    @Override
    public Set<GrantedAuthority> getAuthorities() {
        // Convert roles to GrantedAuthority objects (authorities)
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role.name())  // Prefixing with "ROLE_" for Spring Security
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
        return true;  // Assume account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Assume account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Assume credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return true;  // Assume account is enabled
    }
}
