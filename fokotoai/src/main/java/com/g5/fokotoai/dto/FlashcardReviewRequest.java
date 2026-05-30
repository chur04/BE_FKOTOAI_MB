package com.g5.fokotoai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardReviewRequest {


    @NotNull(message = "Vocab ID không được để trống")
    Long vocabId;


    @NotNull(message = "Chapter ID không được để trống")
    Long chapterId;


    @NotNull(message = "Kết quả đánh giá (isMastered) không được để trống")
    Boolean isMastered;
}
