package com.g5.fokotoai.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class WeakVocabItem {
    Long vocabId;
    String word;
    Integer errorCount;
    Double weaknessScore;
}