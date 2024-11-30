package com.example.springrestful.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationRequest {
    @NotBlank(message = "Organization name is required")
    private String name;

    private String address;

    private String registrationNumber;
}
