package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findTop5ByStudentStudentIdOrderBySubmittedAtDesc(Long studentId);
}
