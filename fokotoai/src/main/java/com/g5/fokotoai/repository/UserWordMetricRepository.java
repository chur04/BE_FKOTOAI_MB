package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.UserWordMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWordMetricRepository extends JpaRepository<UserWordMetric, Long> {

    @Query("SELECT m FROM UserWordMetric m WHERE m.student.studentId = :studentId AND m.vocab.vocabId = :vocabId")
    Optional<UserWordMetric> findByStudentIdAndVocabId(@Param("studentId") Long studentId,
                                                       @Param("vocabId") Long vocabId);

    long countByStudentStudentId(Long studentId);

    long countByStudentStudentIdAndMastered(Long studentId, Boolean mastered);

    @Query("SELECT m FROM UserWordMetric m JOIN FETCH m.vocab " +
           "WHERE m.student.studentId = :studentId " +
           "AND (m.flashcardIncorrect + m.quizIncorrect) > 0 " +
           "AND m.mastered = false " +
           "ORDER BY (m.flashcardIncorrect + m.quizIncorrect) DESC")
    List<UserWordMetric> findWeakVocabByStudentId(@Param("studentId") Long studentId, Pageable pageable);
}
