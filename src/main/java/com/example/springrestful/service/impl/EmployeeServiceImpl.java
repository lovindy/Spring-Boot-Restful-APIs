//package com.example.springrestful.service.impl;
//
//import com.example.springrestful.dto.EmployeeDto;
//import com.example.springrestful.entity.Employee;
//import com.example.springrestful.exception.ResourceNotFoundException;
//import com.example.springrestful.repository.EmployeeRepository;
//import com.example.springrestful.service.EmployeeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class EmployeeServiceImpl implements EmployeeService {
//
//    private final EmployeeRepository employeeRepository;
//
//    @Override
//    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
//        Employee employee = mapToEntity(employeeDto);
//        Employee savedEmployee = employeeRepository.save(employee);
//        return mapToDto(savedEmployee);
//    }
//
//    @Override
//    public Employee getEmployeeById(Long employeeId) {
//        return employeeRepository.findById(employeeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
//    }
//
//    @Override
//    public List<EmployeeDto> getAllEmployees(Pageable pageable) {
//        return employeeRepository.findAll(pageable)
//                .getContent()
//                .stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public EmployeeDto updateEmployee(Long employeeId, EmployeeDto employeeDto) {
//        Employee employee = getEmployeeById(employeeId);
//
//        // Update employee fields
//        employee.setFirstName(employeeDto.getFirstName());
//        employee.setLastName(employeeDto.getLastName());
//        employee.setEmail(employeeDto.getEmail());
//        employee.setPhoneNumber(employeeDto.getPhoneNumber());
//        employee.setDepartment(employeeDto.getDepartment());
//        employee.setPosition(employeeDto.getPosition());
//
//        Employee updatedEmployee = employeeRepository.save(employee);
//        return mapToDto(updatedEmployee);
//    }
//
//    @Override
//    public EmployeeDto deleteEmployee(Long employeeId) {
//        Employee employee = getEmployeeById(employeeId);
//        employeeRepository.delete(employee);
//        return mapToDto(employee);
//    }
//
//    // Helper method to convert Entity to DTO
//    private EmployeeDto mapToDto(Employee employee) {
//        return EmployeeDto.builder()
//                .id(employee.getId())
//                .firstName(employee.getFirstName())
//                .lastName(employee.getLastName())
//                .email(employee.getEmail())
//                .phoneNumber(employee.getPhoneNumber())
//                .department(employee.getDepartment())
//                .position(employee.getPosition())
//                .build();
//    }
//
//    // Helper method to convert DTO to Entity
//    private Employee mapToEntity(EmployeeDto employeeDto) {
//        return Employee.builder()
//                .firstName(employeeDto.getFirstName())
//                .lastName(employeeDto.getLastName())
//                .email(employeeDto.getEmail())
//                .phoneNumber(employeeDto.getPhoneNumber())
//                .department(employeeDto.getDepartment())
//                .position(employeeDto.getPosition())
//                .build();
//    }
//}