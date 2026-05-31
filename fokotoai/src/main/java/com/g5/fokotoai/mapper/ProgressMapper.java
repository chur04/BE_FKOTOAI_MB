package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.response.WeakVocabResponse;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.entity.Vocabulary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProgressMapper {

    @Mapping(target = "totalWrongCount", expression = "java(calculateTotalWrongCount(metric))")
    WeakVocabResponse toWeakVocabResponse(Vocabulary vocab, UserWordMetric metric);

    default Integer calculateTotalWrongCount(UserWordMetric metric) {
        if (metric == null) return null;
        int flashcardIncorrect = metric.getFlashcardIncorrect() != null ? metric.getFlashcardIncorrect() : 0;
        int quizIncorrect = metric.getQuizIncorrect() != null ? metric.getQuizIncorrect() : 0;
        return flashcardIncorrect + quizIncorrect;
    }
}
