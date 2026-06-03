package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FlashcardReviewRequest {
    @NotNull(message = "VOCAB_ID_REQUIRED")
    Long vocabId;


    @NotNull(message = "CHAPTER_ID_REQUIRED")
    Long chapterId;


    @NotNull(message = "IS_MASTERED_REQUIRED")
    Boolean isMastered;
}
