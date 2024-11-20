package com.example.springrestful.mapper;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;

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
        return new Employee(
                employeeDto.getId(),
                employeeDto.getFirstName(),
                employeeDto.getLastName(),
                employeeDto.getEmail()
        );
    }
}
