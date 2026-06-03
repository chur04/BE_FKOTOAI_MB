package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.VocabularyChapterRequest;
import com.g5.fokotoai.dto.response.VocabularyChapterResponse;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.VocabularyChapter;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.VocabularyChapterMapper;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.VocabularyChapterRepository;
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
public class VocabularyChapterService {

    VocabularyChapterRepository chapterRepository;
    StudentRepository studentRepository;
    VocabularyChapterMapper chapterMapper;

    @Transactional
    public VocabularyChapterResponse createChapterForStudent(Long studentId, VocabularyChapterRequest request) {
        Student student = findStudentOrThrow(studentId);

        VocabularyChapter chapter = VocabularyChapter.builder()
                .student(student)
                .chapterName(request.getChapterName())
                .level(request.getLevel())
                .orderIndex(request.getOrderIndex())
                .description(request.getDescription())
                .build();

        return chapterMapper.toResponse(chapterRepository.save(chapter));
    }

    @Transactional(readOnly = true)
    public List<VocabularyChapterResponse> getMyChapters(Long studentId) {
        findStudentOrThrow(studentId);
        return chapterRepository.findByStudentStudentId(studentId)
                .stream()
                .map(chapterMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VocabularyChapterResponse> getSystemChapters() {
        return chapterRepository.findByStudentIsNull()
                .stream()
                .map(chapterMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VocabularyChapterResponse getChapterDetail(Long chapterId, Long studentId) {
        VocabularyChapter chapter = findChapterOrThrow(chapterId);

        boolean isPrivate = chapter.getStudent() != null;
        if (isPrivate) {
            verifyOwnership(chapter, studentId);
        }

        return chapterMapper.toResponse(chapter);
    }

    @Transactional
    public VocabularyChapterResponse updateChapterForStudent(Long chapterId,
                                                             Long studentId,
                                                            VocabularyChapterRequest request) {
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyPrivateAndOwnership(chapter, studentId);

        chapter.setChapterName(request.getChapterName());
        chapter.setLevel(request.getLevel());
        chapter.setOrderIndex(request.getOrderIndex());
        chapter.setDescription(request.getDescription());

        return chapterMapper.toResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public void deleteChapterForStudent(Long chapterId, Long studentId) {
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyPrivateAndOwnership(chapter, studentId);
        chapterRepository.delete(chapter);
    }

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private VocabularyChapter findChapterOrThrow(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    private void verifyPrivateAndOwnership(VocabularyChapter chapter, Long studentId) {
        if (chapter.getStudent() == null) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
        verifyOwnership(chapter, studentId);
    }

    private void verifyOwnership(VocabularyChapter chapter, Long studentId) {
        if (!chapter.getStudent().getStudentId().equals(studentId)) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }

}
