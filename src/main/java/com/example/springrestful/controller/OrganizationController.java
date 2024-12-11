package com.example.springrestful.controller;

import com.example.springrestful.dto.OrganizationRequest;
import com.example.springrestful.dto.OrganizationResponse;
import com.example.springrestful.entity.Organization;
import com.example.springrestful.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Updated to match the actual role name
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody OrganizationRequest request) throws AccessDeniedException {

        Organization organization = Organization.builder()
                .name(request.getName())
                .address(request.getAddress())
                .registrationNumber(request.getRegistrationNumber())
                .build();

        Organization savedOrganization = organizationService.createOrganization(organization);
        return ResponseEntity.ok(OrganizationResponse.fromEntity(savedOrganization));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')") // Updated to match the actual role names
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(OrganizationResponse.fromEntity(organization));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizationsByAdmin() throws AccessDeniedException {
        List<Organization> organizations = organizationService.getOrganizationsByAdmin();
        List<OrganizationResponse> responses = organizations.stream()
                .map(OrganizationResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }
}