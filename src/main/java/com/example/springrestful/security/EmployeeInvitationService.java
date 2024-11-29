package com.example.springrestful.security;

import com.example.springrestful.entity.EmployeeInvitation;
import com.example.springrestful.entity.Organization;
import com.example.springrestful.exception.InvalidInvitationException;
import com.example.springrestful.repository.EmployeeInvitationRepository;
import com.example.springrestful.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeInvitationService {
    private final EmployeeInvitationRepository invitationRepository;
    private final OrganizationService organizationService;
    private final EmailService emailService;

    @Transactional
    public EmployeeInvitation createInvitation(Long organizationId, String email) {
        Organization organization = organizationService.getOrganizationById(organizationId);

        EmployeeInvitation invitation = EmployeeInvitation.builder()
                .email(email)
                .organization(organization)
                .invitationToken(UUID.randomUUID().toString())
                .tokenExpiry(LocalDateTime.now().plusDays(7))
                .status(EmployeeInvitation.InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        EmployeeInvitation savedInvitation = invitationRepository.save(invitation);
        emailService.sendInvitationEmail(savedInvitation);

        return savedInvitation;
    }

    @Transactional
    public void acceptInvitation(String token) {
        EmployeeInvitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token"));

        if (invitation.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidInvitationException("Invitation has expired");
        }

        if (invitation.getStatus() != EmployeeInvitation.InvitationStatus.PENDING) {
            throw new InvalidInvitationException("Invitation is no longer valid");
        }

        invitation.setStatus(EmployeeInvitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }
}
