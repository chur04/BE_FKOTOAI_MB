package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class FlashCardProgressResponse {
    long totalStudied;
    long totalMastered;
    long totalInProgress;
    double masteredPercent;
}
