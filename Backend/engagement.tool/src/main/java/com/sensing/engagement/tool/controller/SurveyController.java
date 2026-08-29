package com.sensing.engagement.tool.controller;

import com.sensing.engagement.tool.dto.CreateSurveyRequest;
import com.sensing.engagement.tool.dto.SubmitAnswerRequest;
import com.sensing.engagement.tool.dto.SurveyResponse;
import com.sensing.engagement.tool.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    public ResponseEntity<SurveyResponse> createSurvey(@RequestBody CreateSurveyRequest request) {

        SurveyResponse response = surveyService.createSurvey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<SurveyResponse> getMySurvey(@RequestParam String employeeId) {

        return ResponseEntity.ok(surveyService.getMySurvey(employeeId));
    }

    @GetMapping("/{surveyId}")
    public ResponseEntity<SurveyResponse> getSurvey(@PathVariable UUID surveyId, @RequestParam String employeeId) {

        return ResponseEntity.ok(surveyService.getSurvey(surveyId, employeeId));
    }

    @PostMapping("/{surveyId}/start")
    public ResponseEntity<SurveyResponse> startSurvey(@PathVariable UUID surveyId, @RequestParam String employeeId) {

        return ResponseEntity.ok(surveyService.startSurvey(surveyId,employeeId));
    }

    @PostMapping("/{surveyId}/answers")
    public ResponseEntity<SurveyResponse> submitAnswer(@PathVariable UUID surveyId, @RequestParam String employeeId, @RequestBody SubmitAnswerRequest request) {

        return ResponseEntity.ok(surveyService.submitAnswer(surveyId, employeeId,request));
    }

    @PostMapping("/{surveyId}/complete")
    public ResponseEntity<SurveyResponse> completeSurvey(@PathVariable UUID surveyId, @RequestParam String employeeId) {

        return ResponseEntity.ok(surveyService.completeSurvey(surveyId, employeeId));
    }
}