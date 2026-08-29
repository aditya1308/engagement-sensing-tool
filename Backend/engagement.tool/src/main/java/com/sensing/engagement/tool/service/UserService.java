package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User registerUser(User user) {

        if (user.getEmployeeId() == null || user.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getDateOfJoining() == null) {
            throw new IllegalArgumentException("Date of joining is required");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("Role is required");
        }

        if (userRepository.existsByEmployeeId(user.getEmployeeId())) {
            throw new IllegalArgumentException(
                    "Employee ID already exists: " + user.getEmployeeId()
            );
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists: " + user.getEmail()
            );
        }
        return userRepository.save(user);
    }
}