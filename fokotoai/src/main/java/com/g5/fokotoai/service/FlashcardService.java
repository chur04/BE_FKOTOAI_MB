package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.FlashcardResponse;
import com.g5.fokotoai.dto.FlashcardReviewRequest;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.entity.Vocabulary;
import com.g5.fokotoai.entity.VocabularyChapterItem;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.UserWordMetricRepository;
import com.g5.fokotoai.repository.VocabularyChapterItemRepository;
import com.g5.fokotoai.repository.VocabularyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * UC-06: Study Flashcards
 * - Lấy danh sách thẻ học theo chapter.
 * - Xử lý Upsert kết quả đánh giá từng thẻ.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashcardService {

    VocabularyChapterItemRepository chapterItemRepository;
    UserWordMetricRepository userWordMetricRepository;
    StudentRepository studentRepository;
    VocabularyRepository vocabularyRepository;

    // ──────────────────────────────────────────────────────────────
    // UC-06a: Lấy danh sách thẻ học của một Chapter
    // ──────────────────────────────────────────────────────────────

    /**
     * Trả về danh sách flashcard của chapter, kèm trạng thái học hiện tại của student.
     *
     * @param studentId ID của học sinh (lấy từ token)
     * @param chapterId ID của chapter cần học
     * @return danh sách FlashcardResponse, sắp xếp theo order_index
     */
    @Transactional(readOnly = true)
    public List<FlashcardResponse> getFlashcardsByChapter(Long studentId, Long chapterId) {
        // Lấy danh sách items trong chapter (JOIN FETCH vocab, đã sort theo order_index)
        List<VocabularyChapterItem> items = chapterItemRepository.findByChapterIdWithVocab(chapterId);

        return items.stream()
                .map(item -> {
                    Vocabulary vocab = item.getVocab();

                    // Lấy metric của student cho từ vựng này (nếu đã học)
                    UserWordMetric metric = userWordMetricRepository
                            .findByStudentIdAndVocabId(studentId, vocab.getVocabId())
                            .orElse(null);

                    return toFlashcardResponse(item, vocab, metric);
                })
                .toList();
    }

    // ──────────────────────────────────────────────────────────────
    // UC-06b: Nộp kết quả đánh giá một thẻ (Upsert)
    // ──────────────────────────────────────────────────────────────

    /**
     * Upsert kết quả học flashcard:
     * - Nếu chưa có metric → tạo mới.
     * - Nếu đã có → cộng dồn flashcard_correct hoặc flashcard_incorrect, cập nhật mastered và last_reviewed_at.
     *
     * @param studentId ID của học sinh (lấy từ token)
     * @param request   dữ liệu đánh giá từ học sinh
     */
    @Transactional
    public FlashcardResponse submitFlashcardReview(Long studentId, FlashcardReviewRequest request) {
        // 1. Kiểm tra vocab có tồn tại và thuộc đúng chapter không
        if (!chapterItemRepository.existsByChapterChapterIdAndVocabVocabId(
                request.getChapterId(), request.getVocabId())) {
            throw new AppException(ErrorCode.VOCAB_NOT_IN_CHAPTER);
        }

        // 2. Tải Student và Vocabulary
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Vocabulary vocab = vocabularyRepository.findById(request.getVocabId())
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_NOT_FOUND));

        // 3. Upsert UserWordMetric
        UserWordMetric metric = userWordMetricRepository
                .findByStudentIdAndVocabId(studentId, vocab.getVocabId())
                .orElseGet(() -> UserWordMetric.builder()
                        .student(student)
                        .vocab(vocab)
                        .mastered(false)
                        .flashcardCorrect(0)
                        .flashcardIncorrect(0)
                        .quizCorrect(0)
                        .quizIncorrect(0)
                        .build());

        // 4. Cộng dồn counter và cập nhật trạng thái
        if (Boolean.TRUE.equals(request.getIsMastered())) {
            metric.setFlashcardCorrect(metric.getFlashcardCorrect() + 1);
            metric.setMastered(true);
        } else {
            metric.setFlashcardIncorrect(metric.getFlashcardIncorrect() + 1);
            // Nếu trả lời sai thì đánh dấu chưa thuộc lại
            metric.setMastered(false);
        }
        metric.setLastReviewedAt(Instant.now());
        metric.setUpdatedAt(Instant.now());

        UserWordMetric saved = userWordMetricRepository.save(metric);

        // 5. Tìm orderIndex để trả về response đầy đủ
        VocabularyChapterItem item = chapterItemRepository
                .findByChapterIdWithVocab(request.getChapterId())
                .stream()
                .filter(i -> i.getVocab().getVocabId().equals(vocab.getVocabId()))
                .findFirst()
                .orElse(null);

        return toFlashcardResponse(item, vocab, saved);
    }

    // ──────────────────────────────────────────────────────────────
    // Private helper
    // ──────────────────────────────────────────────────────────────

    private FlashcardResponse toFlashcardResponse(VocabularyChapterItem item,
                                                   Vocabulary vocab,
                                                   UserWordMetric metric) {
        return FlashcardResponse.builder()
                .vocabId(vocab.getVocabId())
                .word(vocab.getWord())
                .reading(vocab.getReading())
                .meaning(vocab.getMeaning())
                .partOfSpeech(vocab.getPartOfSpeech())
                .audioUrl(vocab.getAudioUrl())
                .exampleSentence(vocab.getExampleSentence())
                .exampleMeaning(vocab.getExampleMeaning())
                .onyomi(vocab.getOnyomi())
                .kunyomi(vocab.getKunyomi())
                .strokeOrderUrl(vocab.getStrokeOrderUrl())
                .isKanji(vocab.getIsKanji())
                .orderIndex(item != null ? item.getOrderIndex() : null)
                // Metric có thể null nếu học sinh chưa học lần nào
                .mastered(metric != null ? metric.getMastered() : null)
                .flashcardCorrect(metric != null ? metric.getFlashcardCorrect() : null)
                .flashcardIncorrect(metric != null ? metric.getFlashcardIncorrect() : null)
                .totalWrongCount(metric != null ? metric.getTotalWrongCount() : null)
                .build();
    }
}
