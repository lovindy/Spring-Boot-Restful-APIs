package com.example.springrestful.dto;

import com.example.springrestful.entity.Organization;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OrganizationResponse {
    private Long id;
    private String name;
    private String address;
    private String registrationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto owner;

    public static OrganizationResponse fromEntity(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .address(organization.getAddress())
                .registrationNumber(organization.getRegistrationNumber())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .owner(UserDto.builder()
                        .id(organization.getOwner().getId())
                        .email(organization.getOwner().getEmail())
                        .build())
                .build();
    }
}