package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.ExamTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamTemplateRepository extends JpaRepository<ExamTemplate, Long> {
    List<ExamTemplate> findByCategoryCategoryId(Long categoryId);
}
