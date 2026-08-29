package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {

        User savedUser = userService.registerUser(user);
        log.info("User registered successfully: {}", savedUser.getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}