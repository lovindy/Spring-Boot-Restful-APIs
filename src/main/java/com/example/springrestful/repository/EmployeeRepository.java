package com.example.springrestful.repository;

import com.example.springrestful.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

// Once the EmployeeRepository interface extends JpaRepository,
// it will have ability to perform CRUD on Employee table
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}
