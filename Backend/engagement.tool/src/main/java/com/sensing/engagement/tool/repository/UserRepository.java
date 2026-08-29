package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.enums.Role;
import com.sensing.engagement.tool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmployeeId(String employeeId);
    Optional<User> findByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmail(String email);
    long countByRole(Role role);
}