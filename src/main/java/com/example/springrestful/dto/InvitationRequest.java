package com.example.springrestful.dto;

import lombok.Data;

@Data
public class InvitationRequest {
    private Long organizationId;
    private String email;
}
