package com.g5.fokotoai.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class FlashcardResponse {
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
    Boolean mastered;
    Integer flashcardCorrect;
    Integer flashcardIncorrect;
    Integer totalWrongCount;
}
