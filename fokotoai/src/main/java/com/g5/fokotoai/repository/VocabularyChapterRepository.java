package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.VocabularyChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyChapterRepository extends JpaRepository<VocabularyChapter, Long> {

    /**
     * Lấy tất cả Chapter Cá nhân (Private) của một Student.
     * SQL: SELECT * FROM vocabulary_chapters WHERE student_id = ?
     * (path: student → studentId, Spring Data tự dịch thành findByStudentStudentId)
     */
    List<VocabularyChapter> findByStudentStudentId(Long studentId);

    /**
     * Lấy tất cả Chapter Hệ thống (System/Global) — student_id IS NULL.
     * SQL: SELECT * FROM vocabulary_chapters WHERE student_id IS NULL
     */
    List<VocabularyChapter> findByStudentIsNull();
}
