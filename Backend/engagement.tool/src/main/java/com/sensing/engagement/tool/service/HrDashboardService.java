package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.SurveyResponse;
import com.sensing.engagement.tool.dto.hr.*;
import com.sensing.engagement.tool.enums.Role;
import com.sensing.engagement.tool.enums.SurveyStatus;
import com.sensing.engagement.tool.model.Answer;
import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.repository.AnswerRepository;
import com.sensing.engagement.tool.repository.SurveyRepository;
import com.sensing.engagement.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HrDashboardService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final AnswerRepository answerRepository;


    public DashboardResponse getDashboard() {

        long totalEmployees = userRepository.countByRole(Role.EMPLOYEE);
        long totalSurveys = surveyRepository.count();
        long completedSurveys = surveyRepository.countByStatus(SurveyStatus.COMPLETED);
        long pendingSurveys = surveyRepository.countByStatus(SurveyStatus.PENDING);

        double responseRate = 0.0;

        if (totalSurveys > 0) {
            responseRate = ((double) completedSurveys / totalSurveys) * 100;
        }

        Double averageScore = answerRepository.findOverallAverageScore();

        double overallEngagementScore = averageScore != null ? averageScore : 0.0;

        List<ThemeMetricResponse> themeMetrics =
                answerRepository
                        .findThemeScores()
                        .stream()
                        .map(row ->
                                ThemeMetricResponse.builder()
                                        .theme((String) row[0])
                                        .score(
                                                row[1] != null
                                                        ? ((Number) row[1])
                                                        .doubleValue()
                                                        : 0.0
                                        )
                                        .build()
                        )
                        .toList();


        return DashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .totalSurveys(totalSurveys)
                .completedSurveys(completedSurveys)
                .pendingSurveys(pendingSurveys)
                .responseRate(round(responseRate))
                .overallEngagementScore(
                        round(overallEngagementScore)
                )
                .themeMetrics(themeMetrics)
                .build();
    }

    public List<ThemeMetricResponse> getThemeMetrics() {

        return answerRepository
                .findThemeScores()
                .stream()
                .map(row ->
                        ThemeMetricResponse.builder()
                                .theme((String) row[0])
                                .score(
                                        round(
                                                ((Number) row[1])
                                                        .doubleValue()
                                        )
                                )
                                .build()
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeSurveyResponse getEmployeeResponses(UUID userId) {

        User employee = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Employee not found"
                        )
                );

        if (employee.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested user is not an employee"
            );
        }

        List<Answer> answers =
                answerRepository.findBySurvey_User_Id(userId);

        Map<UUID, SurveyResponseAns> surveyMap =
                new LinkedHashMap<>();


        for (Answer answer : answers) {

            Survey survey = answer.getSurvey();

            SurveyResponseAns surveyResponse =
                    surveyMap.computeIfAbsent(
                            survey.getId(),
                            surveyId ->
                                    SurveyResponseAns.builder()
                                            .surveyId(survey.getId())
                                            .status(survey.getStatus())
                                            .startedAt(
                                                    survey.getStartedAt()
                                            )
                                            .completedAt(
                                                    survey.getCompletedAt()
                                            )
                                            .assignedAt(survey.getAssignedAt())
                                            .answers(
                                                    new java.util.ArrayList<>()
                                            )
                                            .build()
                    );


            AnswerResponse answerResponse =
                    AnswerResponse.builder()
                            .questionId(
                                    answer.getQuestion().getId()
                            )
                            .question(
                                    answer.getQuestion().getQuestion()
                            )
                            .theme(
                                    answer.getQuestion()
                                            .getTheme()
                                            .getName()
                            )
                            .questionType(
                                    answer.getQuestion()
                                            .getQuestionType()
                            )
                            .answer(
                                    answer.getAnswer()
                            )
                            .score(
                                    answer.getScore()
                            )
                            .build();


            surveyResponse
                    .getAnswers()
                    .add(answerResponse);
        }


        return EmployeeSurveyResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .surveys(
                        surveyMap.values()
                                .stream()
                                .toList()
                )
                .build();
    }


    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}