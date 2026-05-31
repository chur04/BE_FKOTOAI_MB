package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.FlashcardResponse;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.entity.Vocabulary;
import com.g5.fokotoai.entity.VocabularyChapterItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FlashcardMapper {

    // MapStruct sẽ tự động tự động tìm kiếm các trường có TÊN GIỐNG NHAU 
    // từ 3 object (item, vocab, metric) để map vào FlashcardResponse.
    // Chỉ cần chỉ định @Mapping cho trường tính toán custom:
    @Mapping(target = "totalWrongCount", expression = "java(calculateTotalWrongCount(metric))")
    FlashcardResponse toResponse(VocabularyChapterItem item, Vocabulary vocab, UserWordMetric metric);

    default Integer calculateTotalWrongCount(UserWordMetric metric) {
        if (metric == null) return null;
        int flashcardIncorrect = metric.getFlashcardIncorrect() != null ? metric.getFlashcardIncorrect() : 0;
        int quizIncorrect = metric.getQuizIncorrect() != null ? metric.getQuizIncorrect() : 0;
        return flashcardIncorrect + quizIncorrect;
    }
}
