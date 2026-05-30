package com.g5.fokotoai.dto.response;

import com.g5.fokotoai.dto.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentHomeResponse {
    StudentProfileDTO studentProfile;
    LearningProgressDTO learningProgress;
    ContinueChapterDTO continueChapter;
    List<RecentQuizDTO> recentQuizzes;
    Long unreadNotificationsCount;
    Integer nationalRank;
    Integer weeklyRank;
    AIChallengeDTO aiChallenge;
}
