package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.enums.SurveyStatus;
import com.sensing.engagement.tool.model.Answer;
import com.sensing.engagement.tool.model.Question;
import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.model.User;
import com.sensing.engagement.tool.repository.AnswerRepository;
import com.sensing.engagement.tool.repository.QuestionRepository;
import com.sensing.engagement.tool.repository.SurveyRepository;
import com.sensing.engagement.tool.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyAssignmentService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public void assignDueSurveys() {

        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (isSurveyDue(user, today)) {
                createSurvey(user);
            }
        }
    }

    private boolean isSurveyDue(User user, LocalDate today) {

        var latestSurvey = surveyRepository.findTopByUserIdOrderByAssignedAtDesc(user.getId());
        if (latestSurvey.isEmpty()) {
            LocalDate firstSurveyDate = user.getDateOfJoining().plusMonths(6);
            return !firstSurveyDate.isAfter(today);
        }

        LocalDate nextSurveyDate = latestSurvey.get()
                        .getAssignedAt()
                        .toLocalDate()
                        .plusMonths(6);
        return !nextSurveyDate.isAfter(today);
    }

    private void createSurvey(User user) {

        LocalDateTime assignedAt = LocalDateTime.now();

        Survey survey = Survey.builder()
                .user(user)
                .status(SurveyStatus.PENDING)
                .assignedAt(assignedAt)
                .build();

        Survey savedSurvey = surveyRepository.save(survey);
        createAnswers(savedSurvey);
    }

    private void createAnswers(Survey survey) {

        List<Question> questions = questionRepository.findAllByActiveTrue();
        if (questions.isEmpty()) {
            log.info("No questions available for survey {}", survey.getId());
            return;
        }

        List<Answer> answers = questions.stream()
                .map(question -> Answer.builder()
                        .survey(survey)
                        .question(question)
                        .answer(null)
                        .score(null)
                        .build())
                .toList();

        answerRepository.saveAll(answers);
        log.info("Survey {} assigned to employee {}", survey.getId(), survey.getUser().getEmployeeId());
        log.info("{} questions attached to survey {}", answers.size(), survey.getId());
        emailNotificationService.sendSurveyInvitation(survey);
    }
}