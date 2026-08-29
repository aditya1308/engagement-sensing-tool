package com.sensing.engagement.tool.dto;

import com.sensing.engagement.tool.enums.QuestionType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionUploadRequest {

    private String theme;

    private List<QuestionRequest> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionRequest {

        private String question;

        private QuestionType questionType;
    }
}