package com.sensing.engagement.tool.dto.hr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeMetricResponse {

    private String theme;
    private Double score;
}