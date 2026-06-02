package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ContinueChapterResponse {
    Long chapterId;
    String chapterName;
    Long totalWords;
    Long masteredWords;
    Double percentage;
}
