package com.example.springrestful.service.impl;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;
import com.example.springrestful.exception.ResourceNotFoundException;
import com.example.springrestful.mapper.EmployeeMapper;
import com.example.springrestful.repository.EmployeeRepository;
import com.example.springrestful.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    // Calling the ability to perform CRUD on Employee table or entity
    private EmployeeRepository employeeRepository;

    // Record the client request into the database (DTO -> JPA)
    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        // Convert DTO to Entity in order to write into the database entity
        Employee employee = EmployeeMapper.mapToEmployeeJpa(employeeDto);

        // Write data into the database table or entity
        Employee savedEmployee = employeeRepository.save(employee);

        // Return the converted data (JPA -> DTO) to client request
        return EmployeeMapper.mapToEmployeeDto(savedEmployee);
    }

    // Get Employee by ID
    @Override
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + employeeId + " not found"));
    }

    // Get all employees
    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
    }

    // Update employee by ID
    @Override
    public EmployeeDto updateEmployee(Long employeeId, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + employeeId + " not found"));

        // Update on the table field
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setEmail(employeeDto.getEmail());

        // Update the entity with the updatedEmployee variable and return DTO to client
        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(updatedEmployee);
    }

    // Delete Employee by ID
    @Override
    public EmployeeDto deleteEmployee(Long employeeId, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with id " + employeeId + " not found"));

        employeeRepository.delete(employee);
        return EmployeeMapper.mapToEmployeeDto(employee);
    }
}