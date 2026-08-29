package com.sensing.engagement.tool.dto.hr;

import com.sensing.engagement.tool.enums.Role;
import com.sensing.engagement.tool.enums.SurveyStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private UUID id;
    private String employeeId;
    private String name;
    private String email;
    private String department;
    private Role role;
    private SurveyStatus surveyStatus;
}