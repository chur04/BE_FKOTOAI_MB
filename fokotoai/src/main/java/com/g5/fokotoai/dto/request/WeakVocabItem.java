package com.g5.fokotoai.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeakVocabItem {
    private Long vocabId;
    private String word;
    private Integer errorCount;
    private Double weaknessScore;
}

