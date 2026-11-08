package com.example.user_service.repository;

import com.example.user_service.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUsername(String username);
    Optional<Patient> findByUuid(String uuid);
    long countByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
    java.util.List<Patient> findByCreatedAtBetween(java.time.Instant start, java.time.Instant end);
}
