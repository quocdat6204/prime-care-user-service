package com.example.user_service.repository;

import com.example.user_service.model.Doctor;
import com.example.user_service.util.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUsername(String username);
    Optional<Doctor> findByUuid(String uuid);
    long countByStatus(UserStatus status);
    List<Doctor> findByStatusOrderByIdAsc(UserStatus status);
}
