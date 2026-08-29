package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.hr.DashboardResponse;
import com.sensing.engagement.tool.dto.hr.EmployeeSurveyResponse;
import com.sensing.engagement.tool.dto.hr.ThemeMetricResponse;
import com.sensing.engagement.tool.service.HrDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrDashboardController {

    private final HrDashboardService hrDashboardService;


    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(hrDashboardService.getDashboard());
    }

    @GetMapping("/themes")
    public ResponseEntity<List<ThemeMetricResponse>>
    getThemeMetrics() {

        return ResponseEntity.ok(
                hrDashboardService.getThemeMetrics()
        );
    }

    @GetMapping("/employees/{id}/responses")
    public ResponseEntity<EmployeeSurveyResponse>
    getEmployeeResponses(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                hrDashboardService.getEmployeeResponses(id)
        );
    }
}