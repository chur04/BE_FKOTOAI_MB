package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.VocabularyChapterItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyChapterItemRepository extends JpaRepository<VocabularyChapterItem, Long> {
    
    List<VocabularyChapterItem> findByChapterId(Long chapterId);

    long countByChapterId(Long chapterId);

    @Query("SELECT COUNT(i) FROM VocabularyChapterItem i JOIN UserWordMetric m ON i.vocab.id = m.vocab.id WHERE i.chapter.id = :chapterId AND m.student.id = :studentId AND m.mastered = true")
    long countMasteredWordsByChapterIdAndStudentId(@Param("chapterId") Long chapterId, @Param("studentId") Long studentId);
}
