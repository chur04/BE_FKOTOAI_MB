package com.g5.fokotoai.repository;

import com.g5.fokotoai.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    long countByLevel(com.g5.fokotoai.enums.Level level);

    Optional<Vocabulary> findByWordAndMeaning(String word, String meaning);
}
