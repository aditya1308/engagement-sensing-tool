package com.sensing.engagement.tool.service;

import com.sensing.engagement.tool.dto.QuestionResponse;
import com.sensing.engagement.tool.dto.QuestionUploadRequest;
import com.sensing.engagement.tool.model.Question;
import com.sensing.engagement.tool.model.Theme;
import com.sensing.engagement.tool.repository.QuestionRepository;
import com.sensing.engagement.tool.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public List<QuestionResponse> uploadQuestions(QuestionUploadRequest request) {

        validateRequest(request);

        Theme theme = getOrCreateTheme(request.getTheme());

        List<Question> questions = request.getQuestions()
                .stream()
                .map(questionRequest -> Question.builder()
                        .theme(theme)
                        .question(questionRequest.getQuestion())
                        .questionType(questionRequest.getQuestionType())
                        .build())
                .toList();

        List<Question> savedQuestions = questionRepository.saveAll(questions);

        return savedQuestions.stream()
                .map(question -> mapToResponse(question, theme))
                .toList();
    }

    private void validateRequest(QuestionUploadRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getTheme() == null || request.getTheme().isBlank()) {
            throw new IllegalArgumentException("Theme is required");
        }

        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("At least one question is required");
        }
    }

    private Theme getOrCreateTheme(String themeName) {

        return themeRepository.findByNameIgnoreCase(themeName.trim())
                .orElseGet(() -> themeRepository.save(
                        Theme.builder()
                                .name(themeName.trim())
                                .build()
                ));
    }

    private QuestionResponse mapToResponse(Question question, Theme theme) {

        return QuestionResponse.builder()
                .id(question.getId())
                .themeId(theme.getId())
                .theme(theme.getName())
                .question(question.getQuestion())
                .questionType(question.getQuestionType())
                .build();
    }
}