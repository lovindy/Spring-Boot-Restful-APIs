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

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestAttribute Long userId,
            @Valid @RequestBody OrganizationRequest request) {

        Organization organization = Organization.builder()
                .name(request.getName())
                .address(request.getAddress())
                .registrationNumber(request.getRegistrationNumber())
                .build();

        Organization savedOrganization = organizationService.createOrganization(userId, organization);
        return ResponseEntity.ok(OrganizationResponse.fromEntity(savedOrganization));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrganizationResponse> getOrganization(@PathVariable Long id) {
        Organization organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(OrganizationResponse.fromEntity(organization));
    }
}