package com.sensing.engagement.tool.dto;

import com.sensing.engagement.tool.enums.QuestionType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
public class QuestionResponse {

    private UUID id;

    private UUID themeId;

    private String theme;

    private String question;

    private QuestionType questionType;
}