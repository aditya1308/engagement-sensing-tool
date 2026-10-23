package com.sensing.engagement.tool.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitAnswerRequest {

    private UUID answerId;
    private String answer;
    private Double score;
    private String followUpExplanation;
}