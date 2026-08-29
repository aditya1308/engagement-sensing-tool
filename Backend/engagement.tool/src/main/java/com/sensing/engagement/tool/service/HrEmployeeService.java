package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.hr.EmployeeResponse;
import com.sensing.engagement.tool.enums.Role;
import com.sensing.engagement.tool.enums.SurveyStatus;
import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.repository.SurveyRepository;
import com.sensing.engagement.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrEmployeeService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    public List<EmployeeResponse> getEmployees() {

        List<User> employees =
                userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() != null
                                        && user.getRole().name()
                                        .equals(String.valueOf(Role.EMPLOYEE))
                        )
                        .toList();


        return employees.stream()
                .map(this::mapToResponse)
                .toList();
    }


    private EmployeeResponse mapToResponse(User user) {

        Survey latestSurvey =
                surveyRepository
                        .findTopByUserIdOrderByAssignedAtDesc(
                                user.getId()
                        )
                        .orElse(null);


        return EmployeeResponse.builder()

                .id(user.getId())

                .employeeId(
                        user.getEmployeeId()
                )

                .name(
                        user.getName()
                )

                .email(
                        user.getEmail()
                )

                .department(
                        user.getDepartment()
                )

                .role(
                        user.getRole()
                )

                .surveyStatus(
                        latestSurvey != null
                                ? latestSurvey.getStatus()
                                : null
                )

                .build();
    }

    public EmployeeResponse getEmployee(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found with id: " + id)
                );

        SurveyStatus surveyStatus = surveyRepository
                .findTopByUser_IdOrderByStartedAtDesc(id)
                .map(Survey::getStatus)
                .orElse(null);

        return EmployeeResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .role(user.getRole())
                .surveyStatus(surveyStatus)
                .build();
    }
}