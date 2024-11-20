package com.example.springrestful.service.impl;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;
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

    @Override
    public EmployeeDto getEmployeeById(Long employeeId) {
        return null;
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(EmployeeMapper::mapToEmployeeDto)
                .collect(Collectors.toList());
    }
}
