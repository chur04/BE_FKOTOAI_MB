package com.g5.fokotoai.service;


import com.g5.fokotoai.dto.response.*;
import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.entity.*;
import com.g5.fokotoai.repository.*;
import com.g5.fokotoai.enums.Level;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

        StudentProfileResponse profileDTO = StudentProfileResponse.builder()
                .fullname(student.getFullname())
                .avatarUrl(student.getAvatarUrl())
                .streakCount(student.getStreakCount() != null ? student.getStreakCount() : 0)
                .rankPoints(student.getRankPoints() != null ? student.getRankPoints() : 1000)
                .build();

        // 2. Learning Progress
        Level level = student.getCurrentLevel();
        if (level == null) {
            level = Level.N5; // default fallback
        }

        long totalWords = vocabularyRepository.countByLevel(level);
        long masteredWords = userWordMetricRepository.countMasteredByStudentIdAndLevel(studentId, level);
        double overallPercentage = totalWords == 0 ? 0.0 : ((double) masteredWords / totalWords) * 100.0;
        overallPercentage = BigDecimal.valueOf(overallPercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        LearningProgressResponse progressDTO = LearningProgressResponse.builder()
                .totalWords(totalWords)
                .masteredWords(masteredWords)
                .percentage(overallPercentage)
                .build();

        // 3. Continue Chapter
        ContinueChapterResponse continueChapterResponse = null;
        List<VocabularyChapter> chapters = vocabularyChapterRepository.findByLevelOrderByOrderIndexAsc(level);
        VocabularyChapter targetChapter = null;
        long chapterTotal = 0;
        long chapterMastered = 0;

        for (VocabularyChapter ch : chapters) {
            long total = vocabularyChapterItemRepository.countByChapterChapterId(ch.getChapterId());
            if (total == 0) continue;
            long mastered = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(ch.getChapterId(), studentId);
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
            chapterTotal = vocabularyChapterItemRepository.countByChapterChapterId(targetChapter.getChapterId());
            chapterMastered = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(targetChapter.getChapterId(), studentId);
        }

        if (targetChapter != null) {
            double chapterPercentage = chapterTotal == 0 ? 0.0 : ((double) chapterMastered / chapterTotal) * 100.0;
            chapterPercentage = BigDecimal.valueOf(chapterPercentage)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            continueChapterResponse = ContinueChapterResponse.builder()
                    .chapterId(targetChapter.getChapterId())
                    .chapterName(targetChapter.getChapterName())
                    .totalWords(chapterTotal)
                    .masteredWords(chapterMastered)
                    .percentage(chapterPercentage)
                    .build();
        }

        // 4. Recent Quizzes
        List<QuizAttempt> attempts = quizAttemptRepository.findTop5ByStudentStudentIdOrderBySubmittedAtDesc(studentId);
        List<RecentQuizResponse> recentQuizzes = attempts.stream().map(a -> RecentQuizResponse.builder()
                .attemptId(a.getAttemptId())
                .attemptType(a.getAttemptType() != null ? a.getAttemptType().name() : null)
                .score(a.getScore())
                .correctCount(a.getCorrectCount())
                .totalQuestions(a.getTotalQuestions())
                .timeTakenSeconds(a.getTimeTakenSeconds())
                .passFail(a.getPassFail() != null ? a.getPassFail().name() : null)
                .submittedAt(a.getSubmittedAt())
                .build()
        ).collect(Collectors.toList());

        // 5. Unread Notifications Count
        long unreadNotifications = notificationRepository.countByStudentStudentIdAndIsReadFalse(studentId);

        // 6. National Rank Position
        int nationalRank = studentRepository.calculateNationalRank(profileDTO.getRankPoints());

        // 7. Weekly Rank Position
        Integer weeklyRank = leaderboardWeeklyRepository.findFirstByStudentStudentIdOrderByWeekStartDesc(studentId)
                .map(LeaderboardWeekly::getRankPosition)
                .orElse(null);

        // 8. AI Challenge
        long weakWordsCount = userWordMetricRepository.countWeakVocabularies(studentId);
        AIChallengeResponse aiChallengeDTO = AIChallengeResponse.builder()
                .weakWordsCount(weakWordsCount)
                .status(weakWordsCount > 0 ? "READY" : "NO_WEAK_WORDS")
                .build();

        return StudentHomeResponse.builder()
                .studentProfile(profileDTO)
                .learningProgress(progressDTO)
                .continueChapter(continueChapterResponse)
                .recentQuizzes(recentQuizzes)
                .unreadNotificationsCount(unreadNotifications)
                .nationalRank(nationalRank)
                .weeklyRank(weeklyRank)
                .aiChallenge(aiChallengeDTO)
                .build();
    }
}
