package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminExamParticipationResponse {
    Long templateId;
    String templateName;
    long uniqueStudentsCount;
    double participationPercentage;
    long totalAttempts;
}
