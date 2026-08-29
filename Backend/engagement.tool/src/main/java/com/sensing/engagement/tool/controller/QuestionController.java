package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.QuestionResponse;
import com.sensing.engagement.tool.dto.QuestionUploadRequest;
import com.sensing.engagement.tool.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/upload")
    public ResponseEntity<List<QuestionResponse>> uploadQuestions(@RequestBody QuestionUploadRequest request) {

        List<QuestionResponse> response = questionService.uploadQuestions(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}