package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.response.*;
import com.g5.fokotoai.dto.response.StudentHomeResponse.*;
import com.g5.fokotoai.entity.*;
import com.g5.fokotoai.enums.Level;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class StudentHomeService {

    StudentRepository studentRepository ;
    VocabularyRepository vocabularyRepository ;
    UserWordMetricRepository userWordMetricRepository ;
    VocabularyChapterRepository vocabularyChapterRepository ;
    VocabularyChapterItemRepository vocabularyChapterItemRepository ;
    QuizAttemptRepository quizAttemptRepository ;
    NotificationRepository notificationRepository ;
    LeaderboardWeeklyRepository leaderboardWeeklyRepository ;

    @Transactional(readOnly = true)
    public StudentHomeResponse getStudentHomeService(Long studentId) {
        // 1. Get Student Profile
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND)) ;

        StudentProfileResponse profileDTO = StudentProfileResponse.builder()
                .fullname(student.getFullname())
                .avatarUrl(student.getAvatarUrl())
                .streakCount(student.getStreakCount() != null ? student.getStreakCount() : 0)
                .rankPoints(student.getRankPoints() != null ? student.getRankPoints() : 1000)
                .build() ;

        // 2. Learning Progress
        Level level = student.getCurrentLevel() ;
        if (level == null) {
            level = Level.N5 ;
        }

        long totalWords = vocabularyRepository.countByLevel(level) ;
        long masteredWords = userWordMetricRepository.countMasteredByStudentIdAndLevel(studentId, level) ;
        double overallPercentage = totalWords == 0 ? 0.0 : ((double) masteredWords / totalWords) * 100.0 ;
        overallPercentage = BigDecimal.valueOf(overallPercentage)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue() ;

        LearningProgressResponse progressDTO = LearningProgressResponse.builder()
                .totalWords(totalWords)
                .masteredWords(masteredWords)
                .percentage(overallPercentage)
                .build() ;

        // 3. Continue Chapter
        ContinueChapterResponse continueChapterResponse = null ;
        List<VocabularyChapter> chapters = vocabularyChapterRepository.findByLevelOrderByOrderIndexAsc(level) ;
        VocabularyChapter targetChapter = null ;
        long chapterTotalWords = 0 ;
        long chapterMasteredWords = 0 ;

        for (VocabularyChapter chapter : chapters) {
            long totalWordsInChapter = vocabularyChapterItemRepository.countByChapterChapterId(chapter.getChapterId()) ;
            if (totalWordsInChapter == 0) {
                continue ;
            }
            long masteredWordsInChapter = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(chapter.getChapterId(), studentId) ;
            if (masteredWordsInChapter < totalWordsInChapter) {
                targetChapter = chapter ;
                chapterTotalWords = totalWordsInChapter ;
                chapterMasteredWords = masteredWordsInChapter ;
                break ;
            }
        }

        // If all chapters completed, fallback to the last one
        if (targetChapter == null && !chapters.isEmpty()) {
            targetChapter = chapters.get(chapters.size() - 1) ;
            chapterTotalWords = vocabularyChapterItemRepository.countByChapterChapterId(targetChapter.getChapterId()) ;
            chapterMasteredWords = vocabularyChapterItemRepository.countMasteredWordsByChapterIdAndStudentId(targetChapter.getChapterId(), studentId) ;
        }

        if (targetChapter != null) {
            double chapterPercentage = chapterTotalWords == 0 ? 0.0 : ((double) chapterMasteredWords / chapterTotalWords) * 100.0 ;
            chapterPercentage = BigDecimal.valueOf(chapterPercentage)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue() ;

            continueChapterResponse = ContinueChapterResponse.builder()
                    .chapterId(targetChapter.getChapterId())
                    .chapterName(targetChapter.getChapterName())
                    .totalWords(chapterTotalWords)
                    .masteredWords(chapterMasteredWords)
                    .percentage(chapterPercentage)
                    .build() ;
        }

        // 4. Recent Quizzes
        List<QuizAttempt> attempts = quizAttemptRepository.findTop5ByStudentStudentIdOrderBySubmittedAtDesc(studentId) ;
        List<RecentQuizResponse> recentQuizzes = attempts.stream().map(attempt -> RecentQuizResponse.builder()
                .attemptId(attempt.getAttemptId())
                .attemptType(attempt.getAttemptType() != null ? attempt.getAttemptType().name() : null)
                .score(attempt.getScore())
                .correctCount(attempt.getCorrectCount())
                .totalQuestions(attempt.getTotalQuestions())
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .passFail(attempt.getPassFail() != null ? attempt.getPassFail().name() : null)
                .submittedAt(attempt.getSubmittedAt())
                .build()
        ).collect(Collectors.toList()) ;

        // 5. Unread Notifications Count
        long unreadNotifications = notificationRepository.countByStudentStudentIdAndIsReadFalse(studentId) ;

        // 6. National Rank Position
        int nationalRank = studentRepository.calculateNationalRank(profileDTO.getRankPoints()) ;

        // 7. Weekly Rank Position
        Integer weeklyRank = leaderboardWeeklyRepository.findFirstByStudentStudentIdOrderByWeekStartDesc(studentId)
                .map(LeaderboardWeekly::getRankPosition)
                .orElse(null) ;

        // 8. AI Challenge
        long weakWordsCount = userWordMetricRepository.countWeakVocabularies(studentId) ;
        AIChallengeResponse aiChallengeDTO = AIChallengeResponse.builder()
                .weakWordsCount(weakWordsCount)
                .status(weakWordsCount > 0 ? "READY" : "NO_WEAK_WORDS")
                .build() ;

        return StudentHomeResponse.builder()
                .studentProfile(profileDTO)
                .learningProgress(progressDTO)
                .continueChapter(continueChapterResponse)
                .recentQuizzes(recentQuizzes)
                .unreadNotificationsCount(unreadNotifications)
                .nationalRank(nationalRank)
                .weeklyRank(weeklyRank)
                .aiChallenge(aiChallengeDTO)
                .build() ;
    }
}

