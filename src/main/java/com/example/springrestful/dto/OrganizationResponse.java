package com.example.springrestful.dto;

import com.example.springrestful.entity.Organization;
import lombok.Data;

@Data
public class OrganizationResponse {
    private Long id;
    private String name;
    private String address;
    private String registrationNumber;
    private UserResponse owner;

    public static OrganizationResponse fromEntity(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setAddress(organization.getAddress());
        response.setRegistrationNumber(organization.getRegistrationNumber());
        response.setOwner(UserResponse.fromEntity(organization.getOwner()));
        return response;
    }
}
