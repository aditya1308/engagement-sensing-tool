package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.hr.EmployeeResponse;
import com.sensing.engagement.tool.service.HrEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrEmployeeController {

    private final HrEmployeeService hrEmployeeService;


    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {

        return ResponseEntity.ok(hrEmployeeService.getEmployees());
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeResponse> getEmployeesByStatus(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(hrEmployeeService.getEmployee(employeeId));
    }


}