package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.model.Survey;
import com.sensing.engagement.tool.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    List<Survey> findByUserId(UUID userId);
    List<Survey> findByStatus(SurveyStatus status);
    Optional<Survey> findTopByUserIdOrderByAssignedAtDesc(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, SurveyStatus status);
    long countByStatus(SurveyStatus status);
    Optional<Survey> findTopByUser_IdOrderByStartedAtDesc(UUID userId);
}