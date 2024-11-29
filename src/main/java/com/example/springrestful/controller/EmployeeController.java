//package com.example.springrestful.controller;
//
//import com.example.springrestful.dto.EmployeeDto;
//import com.example.springrestful.entity.Employee;
//import com.example.springrestful.service.EmployeeService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/employees")
//@RequiredArgsConstructor
//public class EmployeeController {
//
//    private final EmployeeService employeeService;
//
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<List<EmployeeDto>> getAllEmployees(Pageable pageable) {
//        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
//    public ResponseEntity<Employee> getEmployee(@PathVariable Long id) {
//        return ResponseEntity.ok(employeeService.getEmployeeById(id));
//    }
//
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
//    public ResponseEntity<EmployeeDto> updateEmployee(
//            @PathVariable Long id,
//            @RequestBody @Valid EmployeeDto employeeDto
//    ) {
//        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
//        employeeService.deleteEmployee(id);
//        return ResponseEntity.ok().build();
//    }
//}
//
//
