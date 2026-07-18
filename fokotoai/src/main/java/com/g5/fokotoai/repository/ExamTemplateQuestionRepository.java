package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.ExamTemplateQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamTemplateQuestionRepository extends JpaRepository<ExamTemplateQuestion, Long> {
    
    @Query("SELECT eq FROM ExamTemplateQuestion eq JOIN FETCH eq.question WHERE eq.template.id = :templateId ORDER BY eq.orderIndex ASC")
    List<ExamTemplateQuestion> findByTemplateIdWithQuestions(@Param("templateId") Long templateId);

    @Query("SELECT COALESCE(MAX(eq.orderIndex), 0) FROM ExamTemplateQuestion eq WHERE eq.template.id = :templateId")
    Integer findMaxOrderIndexByTemplateId(@Param("templateId") Long templateId);
}
