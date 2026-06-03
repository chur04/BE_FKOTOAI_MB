package com.g5.fokotoai.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
@Builder
public class StudentHomeResponse {
    StudentProfileResponse studentProfile ;
    LearningProgressResponse learningProgress ;
    ContinueChapterResponse continueChapter ;
    List<RecentQuizResponse> recentQuizzes ;
    Long unreadNotificationsCount ;
    Integer nationalRank ;
    Integer weeklyRank ;
    AIChallengeResponse aiChallenge ;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE )
    @Builder
    public static class StudentProfileResponse {
        String fullname ;
        String avatarUrl ;
        Integer streakCount ;
        Integer rankPoints ;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE )
    @Builder
    public static class LearningProgressResponse {
        Long totalWords ;
        Long masteredWords ;
        Double percentage ;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE )
    @Builder
    public static class ContinueChapterResponse {
        Long chapterId ;
        String chapterName ;
        Long totalWords ;
        Long masteredWords ;
        Double percentage ;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE )
    @Builder
    public static class RecentQuizResponse {
        Long attemptId ;
        String attemptType ;
        BigDecimal score ;
        Integer correctCount ;
        Integer totalQuestions ;
        Integer timeTakenSeconds ;
        String passFail ;
        Instant submittedAt ;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE )
    @Builder
    public static class AIChallengeResponse {
        Long weakWordsCount ;
        String status ;
    }
}
