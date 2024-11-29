package com.example.springrestful.repository;

import com.example.springrestful.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByOwnerId(Long ownerId);

    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);
}

