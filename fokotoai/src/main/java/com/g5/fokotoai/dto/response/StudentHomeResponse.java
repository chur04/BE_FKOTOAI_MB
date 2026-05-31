package com.g5.fokotoai.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class StudentHomeResponse {
    StudentProfileResponse studentProfile;
    LearningProgressResponse learningProgress;
    ContinueChapterResponse continueChapter;
    List<RecentQuizResponse> recentQuizzes;
    Long unreadNotificationsCount;
    Integer nationalRank;
    Integer weeklyRank;
    AIChallengeResponse aiChallenge;
}
