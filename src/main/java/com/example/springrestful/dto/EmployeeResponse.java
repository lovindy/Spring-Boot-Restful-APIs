package com.example.springrestful.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;
    private String position;
    private LocalDate hireDate;
    private boolean isActive;
    private Long adminId;
    private String adminEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
