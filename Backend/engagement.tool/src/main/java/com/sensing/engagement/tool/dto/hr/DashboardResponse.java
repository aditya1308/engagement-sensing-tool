package com.sensing.engagement.tool.dto.hr;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalEmployees;
    private Long totalSurveys;
    private Long completedSurveys;
    private Long pendingSurveys;
    private Double responseRate;
    private Double overallEngagementScore;
    private List<ThemeMetricResponse> themeMetrics;
}