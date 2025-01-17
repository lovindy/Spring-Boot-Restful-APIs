package com.example.springrestful.service;

import com.example.springrestful.entity.EmployeeInvitation;
import com.example.springrestful.entity.Organization;
import com.example.springrestful.exception.InvalidInvitationException;
import com.example.springrestful.repository.EmployeeInvitationRepository;
import com.example.springrestful.security.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmployeeInvitationService {
    private static final String INVITATION_CACHE_PREFIX = "invitation:";
    private static final String INVITATION_TOKEN_PREFIX = "invitation:token:";
    private static final Duration INVITATION_EXPIRE_TIME = Duration.ofDays(7);

    private final EmployeeInvitationRepository invitationRepository;
    private final OrganizationService organizationService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public EmployeeInvitation createInvitation(Long organizationId, String email) {
        Organization organization = organizationService.getOrganizationById(organizationId);
        String token = generateUniqueToken();

        EmployeeInvitation invitation = buildInvitation(email, organization, token);
        EmployeeInvitation savedInvitation = invitationRepository.save(invitation);

        cacheInvitationData(savedInvitation);
        emailService.sendInvitationEmail(savedInvitation);

        return savedInvitation;
    }

    @Transactional
    public void acceptInvitation(String token) {
        // First check Redis cache
        Optional<EmployeeInvitation> cachedInvitation = getInvitationFromCache(token);
        EmployeeInvitation invitation = cachedInvitation.orElseGet(() ->
                invitationRepository.findByInvitationToken(token)
                        .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token"))
        );

        validateInvitation(invitation);
        updateInvitationStatus(invitation);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (Boolean.TRUE.equals(redisTemplate.hasKey(INVITATION_TOKEN_PREFIX + token)));
        return token;
    }

    private EmployeeInvitation buildInvitation(String email, Organization organization, String token) {
        return EmployeeInvitation.builder()
                .email(email)
                .organization(organization)
                .invitationToken(token)
                .tokenExpiry(LocalDateTime.now().plus(INVITATION_EXPIRE_TIME))
                .status(EmployeeInvitation.InvitationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void cacheInvitationData(EmployeeInvitation invitation) {
        String cacheKey = INVITATION_CACHE_PREFIX + invitation.getInvitationToken();
        String tokenKey = INVITATION_TOKEN_PREFIX + invitation.getInvitationToken();

        Map<String, String> invitationData = Map.of(
                "id", String.valueOf(invitation.getId()),
                "email", invitation.getEmail(),
                "organizationId", String.valueOf(invitation.getOrganization().getId()),
                "status", invitation.getStatus().toString(),
                "expiryDate", invitation.getTokenExpiry().toString()
        );

        redisTemplate.opsForHash().putAll(cacheKey, invitationData);
        redisTemplate.opsForValue().set(tokenKey, "1", INVITATION_EXPIRE_TIME);
    }

    private Optional<EmployeeInvitation> getInvitationFromCache(String token) {
        String cacheKey = INVITATION_CACHE_PREFIX + token;
        Map<Object, Object> cachedData = redisTemplate.opsForHash().entries(cacheKey);

        if (cachedData.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(buildInvitationFromCache(cachedData));
    }

    private EmployeeInvitation buildInvitationFromCache(Map<Object, Object> cachedData) {
        Organization organization = organizationService.getOrganizationById(
                Long.parseLong((String) cachedData.get("organizationId"))
        );

        return EmployeeInvitation.builder()
                .id(Long.parseLong((String) cachedData.get("id")))
                .email((String) cachedData.get("email"))
                .organization(organization)
                .invitationToken((String) cachedData.get("token"))
                .status(EmployeeInvitation.InvitationStatus.valueOf((String) cachedData.get("status")))
                .tokenExpiry(LocalDateTime.parse((String) cachedData.get("expiryDate")))
                .build();
    }

    private void validateInvitation(EmployeeInvitation invitation) {
        if (invitation.getTokenExpiry().isBefore(LocalDateTime.now())) {
            invalidateInvitation(invitation.getInvitationToken());
            throw new InvalidInvitationException("Invitation has expired");
        }

        if (invitation.getStatus() != EmployeeInvitation.InvitationStatus.PENDING) {
            throw new InvalidInvitationException("Invitation is no longer valid");
        }
    }

    private void updateInvitationStatus(EmployeeInvitation invitation) {
        invitation.setStatus(EmployeeInvitation.InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());

        EmployeeInvitation updatedInvitation = invitationRepository.save(invitation);
        updateInvitationCache(updatedInvitation);
    }

    private void updateInvitationCache(EmployeeInvitation invitation) {
        String cacheKey = INVITATION_CACHE_PREFIX + invitation.getInvitationToken();
        redisTemplate.opsForHash().put(cacheKey, "status", invitation.getStatus().toString());
        redisTemplate.opsForHash().put(cacheKey, "acceptedAt", invitation.getAcceptedAt().toString());
    }

    private void invalidateInvitation(String token) {
        String cacheKey = INVITATION_CACHE_PREFIX + token;
        String tokenKey = INVITATION_TOKEN_PREFIX + token;

        redisTemplate.delete(cacheKey);
        redisTemplate.delete(tokenKey);
    }

    public List<EmployeeInvitation> findByOrganizationId(Long organizationId) {
        // First check if organization exists
        organizationService.getOrganizationById(organizationId);

        // Get all active (PENDING) invitations for the organization
        return invitationRepository.findByOrganizationIdAndStatus(
                organizationId,
                EmployeeInvitation.InvitationStatus.PENDING
        );
    }

    @Transactional
    public EmployeeInvitation resendInvitation(Long organizationId, String email) {
        // Check for existing pending invitation
        Optional<EmployeeInvitation> existingInvitation = invitationRepository
                .findByEmailAndOrganizationIdAndStatus(
                        email,
                        organizationId,
                        EmployeeInvitation.InvitationStatus.PENDING
                );

        if (existingInvitation.isPresent()) {
            EmployeeInvitation invitation = existingInvitation.get();

            // Check if the existing invitation is close to expiry or expired
            if (invitation.getTokenExpiry().isBefore(LocalDateTime.now().plusDays(1))) {
                // Update existing invitation with new token and expiry
                String newToken = generateUniqueToken();
                invitation.setInvitationToken(newToken);
                invitation.setTokenExpiry(LocalDateTime.now().plus(INVITATION_EXPIRE_TIME));

                EmployeeInvitation updatedInvitation = invitationRepository.save(invitation);

                // Update cache with new token
                invalidateInvitation(invitation.getInvitationToken());
                cacheInvitationData(updatedInvitation);

                // Resend email
                emailService.sendInvitationEmail(updatedInvitation);

                return updatedInvitation;
            } else {
                // If invitation is still valid and not close to expiry, just resend the email
                emailService.sendInvitationEmail(invitation);
                return invitation;
            }
        }

        // If no existing pending invitation, create a new one
        return createInvitation(organizationId, email);
    }

    @Transactional
    public void cancelInvitation(String token) {
        // First check Redis cache
        Optional<EmployeeInvitation> cachedInvitation = getInvitationFromCache(token);
        EmployeeInvitation invitation = cachedInvitation.orElseGet(() ->
                invitationRepository.findByInvitationToken(token)
                        .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token"))
        );

        // Can only cancel PENDING invitations
        if (invitation.getStatus() != EmployeeInvitation.InvitationStatus.PENDING) {
            throw new InvalidInvitationException("Cannot cancel non-pending invitation");
        }

        // Update status to CANCELLED
        invitation.setStatus(EmployeeInvitation.InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);

        // Update cache
        updateInvitationCache(invitation);

        // Optionally, could send an email to the user informing them that
        // their invitation has been cancelled
        // emailService.sendInvitationCancellationEmail(invitation.getEmail());
    }
}