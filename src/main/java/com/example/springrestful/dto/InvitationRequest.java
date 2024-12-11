package com.example.springrestful.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InvitationRequest {
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}