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
    @NotNull(message = "Vocab ID không được để trống")
    Long vocabId;


    @NotNull(message = "Chapter ID không được để trống")
    Long chapterId;


    @NotNull(message = "Kết quả đánh giá (isMastered) không được để trống")
    Boolean isMastered;
}
