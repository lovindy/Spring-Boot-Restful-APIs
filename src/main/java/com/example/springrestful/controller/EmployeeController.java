package com.example.springrestful.controller;

import com.example.springrestful.dto.EmployeeDto;
import com.example.springrestful.entity.Employee;
import com.example.springrestful.response.StandardResponse;
import com.example.springrestful.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("api/employees")
public class EmployeeController {

    // Calling dependencies
    private EmployeeService employeeService;

    // Build Add Employee REST API
    @PostMapping
    public ResponseEntity<StandardResponse<EmployeeDto>> createEmployee(@RequestBody EmployeeDto employeeDto) {
        // Add into the database table or entity
        EmployeeDto savedEmployee = employeeService.createEmployee(employeeDto);

        // Response to the client side using StandardResponse
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.created(savedEmployee));
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<StandardResponse<List<EmployeeDto>>> getAllEmployees() {
        List<EmployeeDto> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(StandardResponse.success(employees));
    }

    // Get employee by id
    @GetMapping("{id}")
    public ResponseEntity<StandardResponse<Employee>> getEmployeeById(@PathVariable("id") Long employeeId) {
        Employee employeeDto = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(StandardResponse.success(employeeDto));
    }

    // Update Employee by ID
    @PutMapping("{id}")
    public ResponseEntity<StandardResponse<EmployeeDto>> updateEmployee(
            @PathVariable("id") long employeeId,
            @RequestBody EmployeeDto employeeDto) {
        EmployeeDto updatedEmployee = employeeService.updateEmployee(employeeId, employeeDto);
        return ResponseEntity.ok(StandardResponse.success(updatedEmployee));
    }

    // Delete Employee by ID
    @DeleteMapping("{id}")
    public ResponseEntity<StandardResponse<EmployeeDto>> deleteEmployee(
            @PathVariable("id") Long employeeId) {
        EmployeeDto deletedEmployee = employeeService.deleteEmployee(employeeId, null);
        return ResponseEntity.ok(StandardResponse.success(deletedEmployee));
    }
}