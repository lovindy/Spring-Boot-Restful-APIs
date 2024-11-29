package com.example.springrestful.dto;

import com.example.springrestful.entity.EmployeeInvitation;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationResponse {
    private Long id;
    private String email;
    private String status;
    private LocalDateTime expiryDate;

    public static InvitationResponse fromEntity(EmployeeInvitation invitation) {
        InvitationResponse response = new InvitationResponse();
        response.setId(invitation.getId());
        response.setEmail(invitation.getEmail());
        response.setStatus(invitation.getStatus().name());
        response.setExpiryDate(invitation.getTokenExpiry());
        return response;
    }
}