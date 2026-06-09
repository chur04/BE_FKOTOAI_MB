package com.g5.fokotoai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AddVocabToChapterRequest {

    @NotBlank(message = "WORD_REQUIRED")
    String word;

    @NotBlank(message = "MEANING_REQUIRED")
    String meaning;
}
