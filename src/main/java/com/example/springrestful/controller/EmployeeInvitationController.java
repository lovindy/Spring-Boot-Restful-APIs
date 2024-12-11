package com.example.springrestful.controller;

import com.example.springrestful.dto.InvitationRequest;
import com.example.springrestful.dto.InvitationResponse;
import com.example.springrestful.entity.EmployeeInvitation;
import com.example.springrestful.service.EmployeeInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class EmployeeInvitationController {
    private final EmployeeInvitationService invitationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InvitationResponse>> getOrganizationInvitations(
            @RequestParam Long organizationId) {
        List<EmployeeInvitation> invitations = invitationService
                .findByOrganizationId(organizationId);
        return ResponseEntity.ok(invitations.stream()
                .map(InvitationResponse::fromEntity)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitationResponse> createInvitation(
            @Valid @RequestBody InvitationRequest request) {

        EmployeeInvitation invitation = invitationService.createInvitation(
                request.getOrganizationId(),
                request.getEmail()
        );

        return ResponseEntity.ok(InvitationResponse.fromEntity(invitation));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(@PathVariable String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{token}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelInvitation(@PathVariable String token) {
        invitationService.cancelInvitation(token);
        return ResponseEntity.ok().build();
    }
}