package com.g5.fokotoai.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class HandwritingRecognitionRequest {

    @NotNull(message = "HANDWRITING_WIDTH_REQUIRED")
    Integer writingAreaWidth;

    @NotNull(message = "HANDWRITING_HEIGHT_REQUIRED")
    Integer writingAreaHeight;

    @NotEmpty(message = "HANDWRITING_INK_EMPTY")
    @Valid
    List<InkStroke> ink;

    @Builder.Default
    Integer maxNumResults = 10;

    @Builder.Default
    String preContext = "";
}
