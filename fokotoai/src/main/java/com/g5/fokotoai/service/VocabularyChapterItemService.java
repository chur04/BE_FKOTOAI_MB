package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.AddVocabToChapterRequest;
import com.g5.fokotoai.dto.response.VocabularyChapterItemResponse;
import com.g5.fokotoai.entity.Vocabulary;
import com.g5.fokotoai.entity.VocabularyChapter;
import com.g5.fokotoai.entity.VocabularyChapterItem;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.VocabularyChapterItemMapper;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.VocabularyChapterItemRepository;
import com.g5.fokotoai.repository.VocabularyChapterRepository;
import com.g5.fokotoai.repository.VocabularyRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabularyChapterItemService {

    VocabularyChapterRepository chapterRepository;
    VocabularyChapterItemRepository chapterItemRepository;
    VocabularyRepository vocabularyRepository;
    StudentRepository studentRepository;
    VocabularyChapterItemMapper chapterItemMapper;


    @Transactional
    public VocabularyChapterItemResponse addVocabToChapter(Long chapterId, Long studentId,
                                                           AddVocabToChapterRequest request) {
        // 1. Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Find chapter & verify ownership
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyOwnership(chapter, studentId);

        // 3. Find existing vocab or create new one (dùng mapper)
        Vocabulary vocab = vocabularyRepository.findByWordAndMeaning(request.getWord(), request.getMeaning())
                .orElseGet(() -> vocabularyRepository.save(
                        chapterItemMapper.toVocabulary(request, chapter)
                ));

        // 4. Check duplicate
        if (chapterItemRepository.existsByChapterChapterIdAndVocabVocabId(chapterId, vocab.getVocabId())) {
            throw new AppException(ErrorCode.VOCAB_ALREADY_IN_CHAPTER);
        }

        // 5. Auto assign orderIndex
        Integer maxOrder = chapterItemRepository.findMaxOrderIndexByChapterId(chapterId);
        int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

        // 6. Save
        VocabularyChapterItem item = VocabularyChapterItem.builder()
                .chapter(chapter)
                .vocab(vocab)
                .orderIndex(nextOrder)
                .build();

        return chapterItemMapper.toResponse(chapterItemRepository.save(item));
    }


    @Transactional
    public void removeVocabFromChapter(Long chapterId, Long vocabId, Long studentId) {
        // 1. Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Find chapter & verify ownership
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyOwnership(chapter, studentId);

        // 3. Check vocab exists in chapter
        if (!chapterItemRepository.existsByChapterChapterIdAndVocabVocabId(chapterId, vocabId)) {
            throw new AppException(ErrorCode.VOCAB_NOT_IN_CHAPTER);
        }

        // 4. Delete
        chapterItemRepository.deleteByChapterChapterIdAndVocabVocabId(chapterId, vocabId);
    }


    @Transactional
    public VocabularyChapterItemResponse updateVocabInChapter(Long chapterId, Long vocabId,
                                                              Long studentId,
                                                              AddVocabToChapterRequest request) {
        // 1. Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Find chapter & verify ownership
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyOwnership(chapter, studentId);

        // 3. Check vocab exists in chapter
        if (!chapterItemRepository.existsByChapterChapterIdAndVocabVocabId(chapterId, vocabId)) {
            throw new AppException(ErrorCode.VOCAB_NOT_IN_CHAPTER);
        }

        // 4. Find vocab & update via mapper
        Vocabulary vocab = vocabularyRepository.findById(vocabId)
                .orElseThrow(() -> new AppException(ErrorCode.VOCABULARY_NOT_FOUND));

        chapterItemMapper.updateVocabularyFromRequest(request, vocab);
        vocabularyRepository.save(vocab);

        // 5. Return updated item
        return chapterItemRepository.findByChapterIdWithVocab(chapterId)
                .stream()
                .filter(item -> item.getVocab().getVocabId().equals(vocabId))
                .map(chapterItemMapper::toResponse)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VOCAB_NOT_IN_CHAPTER));
    }


    @Transactional(readOnly = true)
    public List<VocabularyChapterItemResponse> getVocabsInChapter(Long chapterId, Long studentId) {
        // 1. Verify student exists
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. Find chapter & verify access
        VocabularyChapter chapter = findChapterOrThrow(chapterId);

        // System chapter (student == null) → anyone can view
        // Private chapter → only owner can view
        if (chapter.getStudent() != null) {
            verifyOwnership(chapter, studentId);
        }

        // 3. Fetch with vocab data
        return chapterItemRepository.findByChapterIdWithVocab(chapterId)
                .stream()
                .map(chapterItemMapper::toResponse)
                .toList();
    }



    private VocabularyChapter findChapterOrThrow(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    private void verifyOwnership(VocabularyChapter chapter, Long studentId) {
        // System chapter → cannot modify
        if (chapter.getStudent() == null) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
        // Private chapter → only owner
        if (!chapter.getStudent().getStudentId().equals(studentId)) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }
}
