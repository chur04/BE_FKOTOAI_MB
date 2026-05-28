package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.*;
import com.g5.fokotoai.dto.response.StudentHomeResponse;
import com.g5.fokotoai.entity.*;
import com.g5.fokotoai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentHomeService {

    private final StudentRepository studentRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserWordMetricRepository userWordMetricRepository;
    private final VocabularyChapterRepository vocabularyChapterRepository;
    private final VocabularyChapterItemRepository vocabularyChapterItemRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final NotificationRepository notificationRepository;
    private final LeaderboardWeeklyRepository leaderboardWeeklyRepository;

    @Transactional(readOnly = true)
    public StudentHomeResponse getStudentHomeData(Long studentId) {
        // 1. Get Student Profile
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        StudentProfileDTO profileDTO = StudentProfileDTO.builder()
                .fullname(student.getFullname())
                .avatarUrl(student.getAvatarUrl())
                .streakCount(student.getStreakCount() != null ? student.getStreakCount() : 0)
                .rankPoints(student.getRankPoints() != null ? student.getRankPoints() : 1000)
                .build();

        // 2. Learning Progress
        String level = student.getCurrentLevel();
        if (level == null) {
            level = "N5"; // default fallback
        }

        long totalWords = vocabularyRepository.countByLevel(level);
        long masteredWords = userWordMetricRepository.countMasteredByStudentIdAndLevel(studentId, level);
        double overallPercentage = totalWords == 0 ? 0.0 : ((double) masteredWords / totalWords) * 100.0;
        overallPercentage = BigDecimal.valueOf(overallPercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        // 3. Continue Chapter
        Long continueChapterId = null;
        String continueChapterName = null;
        Double continueChapterProgress = null;

        List<VocabularyChapter> chapters = vocabularyChapterRepository.findByLevelOrderByOrderIndexAsc(level);
        VocabularyChapter targetChapter = null;
        long chapterTotal = 0;
        long chapterMastered = 0;

        for (VocabularyChapter ch : chapters) {
            long total = vocabularyChapterItemRepository.countByChapterId(ch.getId());
            if (total == 0) continue;
            long mastered = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(ch.getId(), studentId);
            if (mastered < total) {
                targetChapter = ch;
                chapterTotal = total;
                chapterMastered = mastered;
                break;
            }
        }

        // If all chapters completed, fallback to the last one
        if (targetChapter == null && !chapters.isEmpty()) {
            targetChapter = chapters.get(chapters.size() - 1);
            chapterTotal = vocabularyChapterItemRepository.countByChapterId(targetChapter.getId());
            chapterMastered = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(targetChapter.getId(), studentId);
        }

        if (targetChapter != null) {
            double chapterPercentage = chapterTotal == 0 ? 0.0 : ((double) chapterMastered / chapterTotal) * 100.0;
            chapterPercentage = BigDecimal.valueOf(chapterPercentage)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            continueChapterId = targetChapter.getId();
            continueChapterName = targetChapter.getChapterName();
            continueChapterProgress = chapterPercentage;
        }

        // 4. Recent Quizzes
        List<QuizAttempt> attempts = quizAttemptRepository.findTop5ByStudentIdOrderBySubmittedAtDesc(studentId);
        List<RecentQuizDTO> recentQuizzes = attempts.stream().map(a -> RecentQuizDTO.builder()
                .attemptId(a.getId())
                .attemptType(a.getAttemptType())
                .score(a.getScore())
                .passFail(a.getPassFail())
                .submittedAt(a.getSubmittedAt())
                .build()
        ).collect(Collectors.toList());

        // 5. Unread Notifications Count
        long unreadNotifications = notificationRepository.countByStudentIdAndIsReadFalse(studentId);

        // 6. National Rank Position
        int nationalRank = (int) studentRepository.countNationalRankByPoints(student.getRankPoints() != null ? student.getRankPoints() : 1000);

        // 7. Weekly Rank Position
        Integer weeklyRank = leaderboardWeeklyRepository.findFirstByStudentIdOrderByWeekStartDesc(studentId)
                .map(LeaderboardWeekly::getRankPosition)
                .orElse(null);

        // 8. AI Challenge
        long weakWordsCount = userWordMetricRepository.countWeakVocabularies(studentId);
        boolean aiChallengeReady = weakWordsCount > 0;

        return StudentHomeResponse.builder()
                .studentProfile(profileDTO)
                .learningProgress(overallPercentage)
                .continueChapterId(continueChapterId)
                .continueChapterName(continueChapterName)
                .continueChapterProgress(continueChapterProgress)
                .recentQuizzes(recentQuizzes)
                .unreadNotificationsCount(unreadNotifications)
                .nationalRank(nationalRank)
                .weeklyRank(weeklyRank)
                .weakWordsCount(weakWordsCount)
                .aiChallengeReady(aiChallengeReady)
                .build();
    }
}
