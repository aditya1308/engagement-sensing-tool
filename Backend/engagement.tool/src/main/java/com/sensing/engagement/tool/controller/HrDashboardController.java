package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.hr.DashboardResponse;
import com.sensing.engagement.tool.dto.hr.EmployeeSurveyResponse;
import com.sensing.engagement.tool.dto.hr.ThemeMetricResponse;
import com.sensing.engagement.tool.service.HrDashboardService;
import com.sensing.engagement.tool.service.DashboardReportEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrDashboardController {

    private final HrDashboardService hrDashboardService;
    private final DashboardReportEmailService dashboardReportEmailService;


    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(hrDashboardService.getDashboard());
    }


    @PostMapping("/dashboard/report/email")
    public ResponseEntity<Map<String, String>> emailDashboardReport() {
        dashboardReportEmailService.sendDashboardReport();
        return ResponseEntity.ok(Map.of(
                "message", "Dashboard report email sent successfully."
        ));
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