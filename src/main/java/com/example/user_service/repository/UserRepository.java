package com.example.user_service.repository;

import com.example.user_service.model.User;
import com.example.user_service.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndEmail(String username, String email);
    Optional<User> findByUuid(String uuid);

    Page<User> findByRole(Role role, Pageable pageable);

    // Thuần Spring Data JPA (Derived Query Method): Tìm theo Tên HOẶC Email chứa keyword (không phân biệt hoa thường)
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    // Thuần Spring Data JPA: Lọc theo Role VÀ (Tên HOẶC Email chứa keyword)
    Page<User> findByRoleAndFullNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role role1, String fullName, Role role2, String email, Pageable pageable);
}
