package com.sensing.engagement.tool.repository;

import com.sensing.engagement.tool.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findAll();

    List<Question> findAllByActiveTrue();

    @Modifying
    @Query("""
        UPDATE Question q
        SET q.active = false
        WHERE q.active = true
    """)
    int deactivateAllQuestions();
}