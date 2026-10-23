package com.sensing.engagement.tool.dto;

import com.sensing.engagement.tool.enums.QuestionType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnswerResponse {

    private UUID answerId;
    private UUID questionId;
    private String question;
    private String theme;
    private QuestionType questionType;
    private String answer;
    private Double score;
    private String followUpExplanation;
}