package com.g5.fokotoai.dto;

import com.g5.fokotoai.enums.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class VocabularyChapterRequest{

    @NotBlank(message = "Chapter name must not be blank")
    @Size(max = 100, message = "Chapter name must not exceed 100 characters")
    String chapterName;

    @NotNull(message = "Level must not be null")
    Level level;

    @NotNull(message = "Order index must not be null")
    Integer orderIndex;

    String description;
}
