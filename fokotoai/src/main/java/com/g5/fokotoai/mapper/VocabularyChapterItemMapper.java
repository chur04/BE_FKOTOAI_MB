package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.request.AddVocabToChapterRequest;
import com.g5.fokotoai.dto.response.VocabularyChapterItemResponse;
import com.g5.fokotoai.entity.Vocabulary;
import com.g5.fokotoai.entity.VocabularyChapter;
import com.g5.fokotoai.entity.VocabularyChapterItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VocabularyChapterItemMapper {

    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "chapterId", source = "chapter.chapterId")
    @Mapping(target = "vocabId", source = "vocab.vocabId")
    @Mapping(target = "word", source = "vocab.word")
    @Mapping(target = "reading", source = "vocab.reading")
    @Mapping(target = "meaning", source = "vocab.meaning")
    @Mapping(target = "partOfSpeech", source = "vocab.partOfSpeech")
    @Mapping(target = "audioUrl", source = "vocab.audioUrl")
    @Mapping(target = "exampleSentence", source = "vocab.exampleSentence")
    @Mapping(target = "exampleMeaning", source = "vocab.exampleMeaning")
    @Mapping(target = "onyomi", source = "vocab.onyomi")
    @Mapping(target = "kunyomi", source = "vocab.kunyomi")
    @Mapping(target = "strokeOrderUrl", source = "vocab.strokeOrderUrl")
    @Mapping(target = "isKanji", source = "vocab.isKanji")
    VocabularyChapterItemResponse toResponse(VocabularyChapterItem item);

    /**
     * Tạo Vocabulary mới từ request + chapter.
     * reading = word (theo yêu cầu), level = chapter.level.
     */
    @Mapping(target = "vocabId", ignore = true)
    @Mapping(target = "reading", source = "request.word")
    @Mapping(target = "word", source = "request.word")
    @Mapping(target = "meaning", source = "request.meaning")
    @Mapping(target = "level", source = "chapter.level")
    @Mapping(target = "partOfSpeech", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "exampleSentence", ignore = true)
    @Mapping(target = "exampleMeaning", ignore = true)
    @Mapping(target = "onyomi", ignore = true)
    @Mapping(target = "kunyomi", ignore = true)
    @Mapping(target = "strokeOrderUrl", ignore = true)
    @Mapping(target = "isKanji", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Vocabulary toVocabulary(AddVocabToChapterRequest request, VocabularyChapter chapter);

    /**
     * Cập nhật Vocabulary đã có từ request.
     * reading = word (theo yêu cầu).
     */
    @Mapping(target = "vocabId", ignore = true)
    @Mapping(target = "reading", source = "word")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "partOfSpeech", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "exampleSentence", ignore = true)
    @Mapping(target = "exampleMeaning", ignore = true)
    @Mapping(target = "onyomi", ignore = true)
    @Mapping(target = "kunyomi", ignore = true)
    @Mapping(target = "strokeOrderUrl", ignore = true)
    @Mapping(target = "isKanji", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateVocabularyFromRequest(AddVocabToChapterRequest request, @MappingTarget Vocabulary vocab);
}
