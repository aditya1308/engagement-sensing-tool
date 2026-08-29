package com.sensing.engagement.tool.dto;

import com.sensing.engagement.tool.enums.SurveyStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponse {

    private UUID surveyId;
    private SurveyStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<QuestionAnswerResponse> questions;
}