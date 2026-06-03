package com.g5.fokotoai.dto.response;

import com.g5.fokotoai.enums.Level;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder

public class VocabularyChapterResponse {

    Long chapterId;
    Long studentId;
    String chapterName;
    Level level;
    Integer orderIndex;
    String description;
}
