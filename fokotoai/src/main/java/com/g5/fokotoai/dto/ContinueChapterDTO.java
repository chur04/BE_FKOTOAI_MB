package com.g5.fokotoai.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContinueChapterDTO {
    Long chapterId;
    String chapterName;
    Long totalWords;
    Long masteredWords;
    Double percentage;
}
