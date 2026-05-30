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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashcardService {

    VocabularyChapterItemRepository chapterItemRepository;
    UserWordMetricRepository userWordMetricRepository;
    StudentRepository studentRepository;
    VocabularyRepository vocabularyRepository;

    @Transactional(readOnly = true)
    public List<FlashcardResponse> getFlashcardsByChapter(Long studentId, Long chapterId) {
        List<VocabularyChapterItem> items = chapterItemRepository.findByChapterIdWithVocab(chapterId);

        return items.stream()
                .map(item -> {
                    Vocabulary vocab = item.getVocab();
                    UserWordMetric metric = userWordMetricRepository
                            .findByStudentIdAndVocabId(studentId, vocab.getVocabId())
                            .orElse(null);
                    return toFlashcardResponse(item, vocab, metric);
                })
                .toList();
    }

    @Transactional
    public FlashcardResponse submitFlashcardReview(Long studentId, FlashcardReviewRequest request) {
        if (!chapterItemRepository.existsByChapterChapterIdAndVocabVocabId(
                request.getChapterId(), request.getVocabId())) {
            throw new AppException(ErrorCode.VOCAB_NOT_IN_CHAPTER);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Vocabulary vocab = vocabularyRepository.findById(request.getVocabId())
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_NOT_FOUND));

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

        if (Boolean.TRUE.equals(request.getIsMastered())) {
            metric.setFlashcardCorrect(metric.getFlashcardCorrect() + 1);
            metric.setMastered(true);
        } else {
            metric.setFlashcardIncorrect(metric.getFlashcardIncorrect() + 1);
            metric.setMastered(false);
        }
        metric.setLastReviewedAt(Instant.now());
        metric.setUpdatedAt(Instant.now());

        UserWordMetric saved = userWordMetricRepository.save(metric);

        VocabularyChapterItem item = chapterItemRepository
                .findByChapterIdWithVocab(request.getChapterId())
                .stream()
                .filter(i -> i.getVocab().getVocabId().equals(vocab.getVocabId()))
                .findFirst()
                .orElse(null);

        return toFlashcardResponse(item, vocab, saved);
    }

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
                .mastered(metric != null ? metric.getMastered() : null)
                .flashcardCorrect(metric != null ? metric.getFlashcardCorrect() : null)
                .flashcardIncorrect(metric != null ? metric.getFlashcardIncorrect() : null)
                .totalWrongCount(metric != null
                        ? metric.getFlashcardIncorrect() + metric.getQuizIncorrect()
                        : null)
                .build();
    }
}
