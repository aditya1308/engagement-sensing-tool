package com.sensing.engagement.tool.dto.hr;

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
public class SurveyResponseAns {

    private UUID surveyId;

    private SurveyStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    private List<AnswerResponse> answers;
}