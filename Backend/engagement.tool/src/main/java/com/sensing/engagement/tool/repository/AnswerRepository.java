package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    List<Answer> findBySurveyId(UUID surveyId);
    Optional<Answer> findByIdAndSurveyId(UUID answerId, UUID surveyId);

    @Query("""
    SELECT AVG(a.score)
    FROM Answer a
    WHERE a.score IS NOT NULL
""")
    Double findOverallAverageScore();

    @Query("""
    SELECT q.theme.name, AVG(a.score)
    FROM Answer a
    JOIN a.question q
    WHERE a.score IS NOT NULL
    GROUP BY q.theme.name
    ORDER BY q.theme.name
""")
    List<Object[]> findThemeScores();

    List<Answer> findBySurvey_User_Id(UUID userId);
}