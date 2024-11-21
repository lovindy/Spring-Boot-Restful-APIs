package com.example.springrestful.service;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;

import java.util.List;

public interface EmployeeService {

    // Add employee interface
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    // Get employee interface
    Employee getEmployeeById(Long employeeId);

    // Get all employees using utils.List
    List<EmployeeDto> getAllEmployees();

    // Update employee by ID
    EmployeeDto updateEmployee(Long employeeId, EmployeeDto employeeDto);

    // Delete Employee by ID
    EmployeeDto deleteEmployee(Long employeeId, EmployeeDto employeeDto);
}
