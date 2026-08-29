package com.sensing.engagement.tool.dto.hr;

import com.sensing.engagement.tool.dto.SurveyResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSurveyResponse {

    private UUID id;

    private String employeeId;

    private String name;

    private String email;

    private String department;

    private List<SurveyResponseAns> surveys;
}