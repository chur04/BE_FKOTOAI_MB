package com.g5.fokotoai.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentQuizDTO {
    Long attemptId;
    String attemptType;
    BigDecimal score;
    String passFail;
    Instant submittedAt;
}
