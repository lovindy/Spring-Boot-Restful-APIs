package com.example.springrestful.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "employee_invitations")
public class EmployeeInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String invitationToken;

    @Column(nullable = false)
    private LocalDateTime tokenExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acceptedAt;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}