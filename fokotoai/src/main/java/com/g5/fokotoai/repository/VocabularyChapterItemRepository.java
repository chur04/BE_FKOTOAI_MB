package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.VocabularyChapterItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyChapterItemRepository extends JpaRepository<VocabularyChapterItem, Long> {

    @Query("SELECT i FROM VocabularyChapterItem i JOIN FETCH i.vocab WHERE i.chapter.chapterId = :chapterId ORDER BY i.orderIndex ASC")
    List<VocabularyChapterItem> findByChapterIdWithVocab(@Param("chapterId") Long chapterId);

    boolean existsByChapterChapterIdAndVocabVocabId(Long chapterId, Long vocabId);
}
