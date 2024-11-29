package com.example.springrestful.dto;

import lombok.Data;

@Data
public class OrganizationRequest {
    private String name;
    private String address;
    private String registrationNumber;
}
