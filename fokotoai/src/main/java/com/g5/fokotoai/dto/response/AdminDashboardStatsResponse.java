package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsResponse {
    long totalStudents;
    long totalChapters;
    long totalVocabulary;
    long activeStudents;
    long totalPackages;
    double totalRevenue;
    long totalExamTemplates;
    long totalQuestions;
    java.util.Map<String, Long> vocabCountByLevel;
    long totalPassAttempts;
    long totalFailAttempts;
    double averageTimeMinutes;
}
