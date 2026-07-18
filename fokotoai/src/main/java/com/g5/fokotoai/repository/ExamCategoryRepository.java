package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.ExamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {
}
