package com.sensing.engagement.tool.scheduler;

import com.sensing.engagement.tool.service.SurveyAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SurveyScheduler {

    private final SurveyAssignmentService surveyAssignmentService;

    @Scheduled(cron = "0 * * * * *")
    public void assignDueSurveys() {
        log.info("========================================");
        log.info("Survey scheduler executed");
        log.info("========================================");
        surveyAssignmentService.assignDueSurveys();
    }
}