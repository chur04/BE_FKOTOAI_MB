package com.g5.fokotoai.dto;

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
public class WeakVocabResponse {

    Long vocabId;
    String word;
    String reading;
    String meaning;
    String partOfSpeech;
    String audioUrl;

    Integer totalWrongCount;

    Integer flashcardIncorrect;
    
    Integer quizIncorrect;

    Boolean mastered;
}
