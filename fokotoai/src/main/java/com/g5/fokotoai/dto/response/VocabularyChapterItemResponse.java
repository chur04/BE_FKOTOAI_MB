package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VocabularyChapterItemResponse {

    Long itemId;
    Long chapterId;
    Long vocabId;
    String word;
    String reading;
    String meaning;
    String partOfSpeech;
    String audioUrl;
    String exampleSentence;
    String exampleMeaning;
    String onyomi;
    String kunyomi;
    String strokeOrderUrl;
    Boolean isKanji;
    Integer orderIndex;
}
