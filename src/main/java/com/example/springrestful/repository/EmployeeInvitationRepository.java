package com.example.springrestful.repository;

import com.example.springrestful.entity.EmployeeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeInvitationRepository extends JpaRepository<EmployeeInvitation, Long> {
    Optional<EmployeeInvitation> findByInvitationToken(String token);

    List<EmployeeInvitation> findByOrganizationIdAndStatus(Long organizationId, EmployeeInvitation.InvitationStatus status);

    boolean existsByEmailAndOrganizationIdAndStatus(String email, Long organizationId, EmployeeInvitation.InvitationStatus status);

    Optional<EmployeeInvitation> findByEmailAndOrganizationId(String email, Long organizationId);

    Optional<EmployeeInvitation> findByEmailAndOrganizationIdAndStatus(
            String email,
            Long organizationId,
            EmployeeInvitation.InvitationStatus status
    );
}
