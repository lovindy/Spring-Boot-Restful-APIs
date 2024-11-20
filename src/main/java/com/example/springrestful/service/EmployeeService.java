package com.example.springrestful.service;

import com.example.springrestful.dto.EmployeeDto;

import java.util.List;

public interface EmployeeService {

    // Add employee interface
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    // Get employee interface
    EmployeeDto getEmployeeById(Long employeeId);

    // Get all employees using utils.List
    List<EmployeeDto> getAllEmployees();
}
