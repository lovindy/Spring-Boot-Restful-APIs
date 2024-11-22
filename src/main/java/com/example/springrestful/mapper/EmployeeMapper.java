// EmployeeMapper.java
package com.example.springrestful.mapper;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.dto.EmployeeResponse;
import com.example.springrestful.entity.Employee;
import com.example.springrestful.entity.User;
import com.example.springrestful.enums.UserRole;

import java.util.Collections;
import java.util.HashSet;

public class EmployeeMapper {

    public static EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .isActive(employee.isActive())
                .adminId(employee.getAdmin() != null ? employee.getAdmin().getId() : null)
                .adminEmail(employee.getAdmin() != null ? employee.getAdmin().getEmail() : null)
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public static Employee mapToEmployee(EmployeeDto employeeDto, User admin) {
        // Create the user account for the employee
        User employeeUser = User.builder()
                .email(employeeDto.getEmail())
                .username(employeeDto.getUsername())
                .password(employeeDto.getPassword()) // Note: This should be encoded before saving
                .roles(new HashSet<>(Collections.singletonList(UserRole.EMPLOYEE)))
                .emailVerified(false)
                .twoFactorAuthEnabled(false)
                .build();

        // Create the employee entity
        return Employee.builder()
                .user(employeeUser)
                .admin(admin)
                .firstName(employeeDto.getFirstName())
                .lastName(employeeDto.getLastName())
                .email(employeeDto.getEmail())
                .phoneNumber(employeeDto.getPhoneNumber())
                .department(employeeDto.getDepartment())
                .position(employeeDto.getPosition())
                .hireDate(employeeDto.getHireDate())
                .isActive(true)
                .build();
    }

    public static void updateEmployeeFromDto(Employee employee, EmployeeDto employeeDto) {
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setEmail(employeeDto.getEmail());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setPosition(employeeDto.getPosition());
        employee.setHireDate(employeeDto.getHireDate());

        // Update the associated user's email if it changed
        if (!employee.getUser().getEmail().equals(employeeDto.getEmail())) {
            employee.getUser().setEmail(employeeDto.getEmail());
        }
    }
}