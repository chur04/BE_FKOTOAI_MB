package com.g5.fokotoai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionReviewItem {

    Integer questionNumber ;
    String question ;
    List<String> options ;
    String yourAnswer ;
    String correctAnswer ;
    Boolean isCorrect ;
    String aiExplanation ;
}
