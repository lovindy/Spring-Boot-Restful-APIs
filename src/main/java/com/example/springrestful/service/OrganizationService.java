package com.example.springrestful.service;

import com.example.springrestful.entity.Organization;
import com.example.springrestful.entity.User;
import com.example.springrestful.exception.DuplicateRegistrationNumberException;
import com.example.springrestful.exception.ResourceNotFoundException;
import com.example.springrestful.repository.OrganizationRepository;
import com.example.springrestful.security.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final AuthService authService;

    @Transactional
    public Organization createOrganization(Organization organization) throws AccessDeniedException {
        Long userId = authService.getCurrentUserId();
        User owner = userService.getUserEntityById(userId);

        // Check for duplicate registration number before saving
        if (organizationRepository.existsByRegistrationNumber(organization.getRegistrationNumber())) {
            throw new DuplicateRegistrationNumberException(organization.getRegistrationNumber());
        }

        organization.setOwner(owner);
        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());

        Organization savedOrganization = organizationRepository.save(organization);
        owner.getOwnedOrganizations().add(savedOrganization);

        return savedOrganization;
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id: " + id));
    }

    @Transactional
    public Organization updateOrganization(Long id, Organization organizationDetails) {
        Organization organization = getOrganizationById(id);

        organization.setName(organizationDetails.getName());
        organization.setAddress(organizationDetails.getAddress());
        organization.setUpdatedAt(LocalDateTime.now());

        return organizationRepository.save(organization);
    }

    @Transactional(readOnly = true)
    public List<Organization> getOrganizationsByAdmin() throws AccessDeniedException {
        Long userId = authService.getCurrentUserId();
        User admin = userService.getUserEntityById(userId);

        // Retrieve all organizations owned by the admin
        return new ArrayList<>(admin.getOwnedOrganizations());
    }
}
