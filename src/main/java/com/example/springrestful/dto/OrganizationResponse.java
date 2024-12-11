package com.example.springrestful.dto;

import com.example.springrestful.entity.Organization;
import com.example.springrestful.entity.User;
import com.example.springrestful.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private String name;
    private String address;
    private String registrationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OwnerDetails owner;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerDetails {
        private Long id;
        private String username;
        private String email;
        private boolean emailVerified;
        private List<String> roles;
    }

    public static OrganizationResponse fromEntity(Organization organization) {
        if (organization == null) {
            return null;
        }

        User owner = organization.getOwner();
        OwnerDetails ownerDetails = null;

        if (owner != null) {
            ownerDetails = OwnerDetails.builder()
                    .id(owner.getId())
                    .username(owner.getUsername())
                    .email(owner.getEmail())
                    .emailVerified(owner.getEmailVerified())
                    .roles(owner.getRoles().stream()
                            .map(Enum::name)
                            .collect(Collectors.toList()))
                    .build();
        }

        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .address(organization.getAddress())
                .registrationNumber(organization.getRegistrationNumber())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .owner(ownerDetails)
                .build();
    }
}