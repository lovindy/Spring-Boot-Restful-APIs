package com.example.springrestful.mapper;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;
import com.example.springrestful.entity.User;

public class EmployeeMapper {

    // JPA -> DTO
    public static EmployeeDto mapToEmployeeDto(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail()
        );
    }

    // DTO to JPA
    public static Employee mapToEmployeeJpa(EmployeeDto employeeDto) {
        // Create a default User object (replace with actual user retrieval logic)
        User user = new User();
        user.setId(null); // Set user fields as needed
        user.setEmail(employeeDto.getEmail());
        user.setUsername("defaultUsername");
        user.setPassword("defaultPassword");

        return new Employee(
                employeeDto.getId(),
                user,
                employeeDto.getFirstName(),
                employeeDto.getLastName(),
                employeeDto.getEmail(),
                null, // Phone number
                null, // Role
                null, // Hire date
                null, // Department
                true  // Active
        );
    }
}
