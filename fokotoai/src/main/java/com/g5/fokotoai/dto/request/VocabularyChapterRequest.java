package com.g5.fokotoai.dto.request;

import com.g5.fokotoai.enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VocabularyChapterRequest{
    @NotBlank(message = "CHAPTER_NAME_REQUIRED")
    @Size(max = 100, message = "CHAPTER_NAME_TOO_LONG")
    String chapterName;

    @NotNull(message = "LEVEL_REQUIRED")
    Level level;

    @NotNull(message = "ORDER_INDEX_REQUIRED")
    Integer orderIndex;

    String description;
}
