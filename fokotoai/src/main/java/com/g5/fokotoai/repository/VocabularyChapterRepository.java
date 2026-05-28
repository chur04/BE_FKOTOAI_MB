package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.VocabularyChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyChapterRepository extends JpaRepository<VocabularyChapter, Long> {
    List<VocabularyChapter> findByLevelOrderByOrderIndexAsc(String level);
}
