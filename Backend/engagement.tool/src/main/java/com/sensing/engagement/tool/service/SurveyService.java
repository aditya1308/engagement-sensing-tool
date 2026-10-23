package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.CreateSurveyRequest;
import com.sensing.engagement.tool.dto.QuestionAnswerResponse;
import com.sensing.engagement.tool.dto.SubmitAnswerRequest;
import com.sensing.engagement.tool.dto.SurveyResponse;
import com.sensing.engagement.tool.enums.SurveyStatus;
import com.sensing.engagement.tool.model.Answer;
import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.repository.AnswerRepository;
import com.sensing.engagement.tool.repository.SurveyRepository;
import com.sensing.engagement.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;


    @Transactional
    public SurveyResponse createSurvey(CreateSurveyRequest request) {

        if (request == null || request.getEmployeeId() == null || request.getEmployeeId().isBlank()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        User user = userRepository.findByEmployeeId(request.getEmployeeId().trim())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + request.getEmployeeId()));

        boolean surveyAlreadyExists = surveyRepository.existsByUserIdAndStatus(user.getId(),SurveyStatus.PENDING);

        if (surveyAlreadyExists) {
            throw new IllegalArgumentException("Employee already has a pending survey");
        }

        Survey survey = Survey.builder().user(user).status(SurveyStatus.PENDING).build();
        Survey savedSurvey = surveyRepository.save(survey);
        return mapToResponse(savedSurvey);
    }

    private SurveyResponse mapToResponse(Survey survey) {

        List<Answer> answers = answerRepository.findBySurveyId(survey.getId());

        List<QuestionAnswerResponse> questions = answers.stream()
                .map(this::mapAnswer)
                .toList();

        return SurveyResponse.builder()
                .surveyId(survey.getId())
                .status(survey.getStatus())
                .assignedAt(survey.getAssignedAt())
                .startedAt(survey.getStartedAt())
                .completedAt(survey.getCompletedAt())
                .questions(questions)
                .build();
    }

    @Transactional(readOnly = true)
    public SurveyResponse getMySurvey(String employeeId) {

        User user = getUser(employeeId);

        Survey survey = surveyRepository
                .findTopByUserIdOrderByAssignedAtDesc(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No survey assigned to this employee"
                        )
                );

        return buildSurveyResponse(survey);
    }

    @Transactional(readOnly = true)
    public SurveyResponse getSurvey(UUID surveyId, String employeeId) {

        User user = getUser(employeeId);

        Survey survey = surveyRepository
                .findById(surveyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey not found"
                        )
                );

        validateSurveyBelongsToUser(survey, user);

        return buildSurveyResponse(survey);
    }

    @Transactional
    public SurveyResponse startSurvey(UUID surveyId, String employeeId) {

        User user = getUser(employeeId);

        Survey survey = getSurveyForUser(surveyId, user);

        if (survey.getStatus() == SurveyStatus.COMPLETED) {
            throw new IllegalStateException("Survey has already been completed");
        }

        if (survey.getStatus() == SurveyStatus.PENDING) {

            survey.setStatus(SurveyStatus.IN_PROGRESS);
            survey.setStartedAt(LocalDateTime.now());

            surveyRepository.save(survey);
        }

        return buildSurveyResponse(survey);
    }

    @Transactional
    public SurveyResponse submitAnswer(UUID surveyId, String employeeId, SubmitAnswerRequest request) {

        User user = getUser(employeeId);
        Survey survey = getSurveyForUser(surveyId, user);

        if (survey.getStatus() == SurveyStatus.PENDING) {
            throw new IllegalStateException("Please start the survey before submitting answers");
        }

        if (survey.getStatus() == SurveyStatus.COMPLETED) {
            throw new IllegalStateException("Survey has already been completed");
        }

        if (request.getAnswerId() == null) {
            throw new IllegalArgumentException("Answer ID is required");
        }

        Answer answer = answerRepository.findByIdAndSurveyId(request.getAnswerId(), surveyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Answer does not belong to this survey"
                        )
                );

        answer.setAnswer(request.getAnswer());
        answer.setScore(request.getScore());

        String explanation = request.getFollowUpExplanation();
        boolean lowRating = answer.getQuestion().getQuestionType() == com.sensing.engagement.tool.enums.QuestionType.RATING
                && request.getScore() != null
                && request.getScore() < 3;

        if (lowRating && (explanation == null || explanation.isBlank())) {
            throw new IllegalArgumentException("Please explain why you selected a rating below 3");
        }

        if (explanation != null && explanation.length() > 5000) {
            throw new IllegalArgumentException("Follow-up explanation must be 5000 characters or fewer");
        }

        answer.setFollowUpExplanation(lowRating ? explanation.trim() : null);

        answerRepository.save(answer);

        return buildSurveyResponse(survey);
    }

    @Transactional
    public SurveyResponse completeSurvey(UUID surveyId, String employeeId) {

        User user = getUser(employeeId);

        Survey survey = getSurveyForUser(surveyId, user);

        if (survey.getStatus() == SurveyStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Survey has already been completed"
            );
        }

        if (survey.getStatus() == SurveyStatus.PENDING) {
            throw new IllegalStateException(
                    "Survey has not been started"
            );
        }

        List<Answer> answers =
                answerRepository.findBySurveyId(surveyId);

        boolean unanswered = answers.stream()
                .anyMatch(answer ->
                        answer.getAnswer() == null ||
                                answer.getAnswer().isBlank()
                );

        if (unanswered) {
            throw new IllegalStateException(
                    "Please answer all questions before completing the survey"
            );
        }

        survey.setStatus(SurveyStatus.COMPLETED);
        survey.setCompletedAt(LocalDateTime.now());

        surveyRepository.save(survey);

        return buildSurveyResponse(survey);
    }

    private User getUser(String employeeId) {

        return userRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found: " + employeeId
                        )
                );
    }

    private Survey getSurveyForUser(
            UUID surveyId,
            User user
    ) {

        Survey survey = surveyRepository
                .findById(surveyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Survey not found"
                        )
                );

        validateSurveyBelongsToUser(survey, user);

        return survey;
    }

    private void validateSurveyBelongsToUser(
            Survey survey,
            User user
    ) {

        if (!survey.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "This survey does not belong to the employee"
            );
        }
    }

    private SurveyResponse buildSurveyResponse(
            Survey survey
    ) {

        List<Answer> answers =
                answerRepository.findBySurveyId(survey.getId());

        List<QuestionAnswerResponse> questions =
                answers.stream()
                        .map(this::mapAnswer)
                        .toList();

        return SurveyResponse.builder()
                .surveyId(survey.getId())
                .status(survey.getStatus())
                .assignedAt(survey.getAssignedAt())
                .startedAt(survey.getStartedAt())
                .completedAt(survey.getCompletedAt())
                .questions(questions)
                .build();
    }

    private QuestionAnswerResponse mapAnswer(
            Answer answer
    ) {

        return QuestionAnswerResponse.builder()
                .answerId(answer.getId())
                .questionId(answer.getQuestion().getId())
                .question(answer.getQuestion().getQuestion())
                .theme(answer.getQuestion().getTheme().getName())
                .questionType(answer.getQuestion().getQuestionType())
                .answer(answer.getAnswer())
                .score(answer.getScore())
                .followUpExplanation(answer.getFollowUpExplanation())
                .build();
    }
}