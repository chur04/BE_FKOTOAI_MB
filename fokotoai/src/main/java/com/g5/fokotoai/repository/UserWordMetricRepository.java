package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.UserWordMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWordMetricRepository extends JpaRepository<UserWordMetric, Long> {

    @Query("SELECT COUNT(u) FROM UserWordMetric u WHERE u.student.studentId = :studentId AND u.vocab.level = :level AND u.mastered = true")
    long countMasteredByStudentIdAndLevel(@Param("studentId") Long studentId, @Param("level") com.g5.fokotoai.enums.Level level);

    @Query("SELECT u FROM UserWordMetric u WHERE u.student.studentId = :studentId AND u.totalWrongCount > 0 ORDER BY u.totalWrongCount DESC")
    List<UserWordMetric> findWeakVocabularies(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserWordMetric u WHERE u.student.studentId = :studentId AND u.totalWrongCount > 0")
    long countWeakVocabularies(@Param("studentId") Long studentId);
}
