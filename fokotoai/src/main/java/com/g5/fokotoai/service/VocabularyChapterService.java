package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.VocabularyChapterRequest;
import com.g5.fokotoai.dto.VocabularyChapterResponse;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.VocabularyChapter;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
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

    // ----------------------------------------------------------------
    // CREATE — chỉ Student mới tự tạo được Chapter riêng của mình
    // ----------------------------------------------------------------

    @Transactional
    public VocabularyChapterResponse createChapterForStudent(Long studentId,
                                                             VocabularyChapterRequest request) {
        Student student = findStudentOrThrow(studentId);

        VocabularyChapter chapter = VocabularyChapter.builder()
                .student(student)
                .chapterName(request.getChapterName())
                .level(request.getLevel())
                .orderIndex(request.getOrderIndex())
                .description(request.getDescription())
                .createdAt(Instant.now())
                .build();

        return toResponse(chapterRepository.save(chapter));
    }

    // ----------------------------------------------------------------
    // READ — Chapter Cá nhân của Student
    // ----------------------------------------------------------------

    /**
     * Lấy toàn bộ Chapter riêng tư mà Student đó đang sở hữu.
     */
    @Transactional(readOnly = true)
    public List<VocabularyChapterResponse> getMyChapters(Long studentId) {
        findStudentOrThrow(studentId);
        return chapterRepository.findByStudentStudentId(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // READ — Chapter Hệ thống (System/Global) — mọi người đều xem được
    // ----------------------------------------------------------------

    /**
     * Lấy toàn bộ Chapter Hệ thống (student_id IS NULL).
     * Không cần studentId — đây là dữ liệu công khai cho tất cả.
     */
    @Transactional(readOnly = true)
    public List<VocabularyChapterResponse> getSystemChapters() {
        return chapterRepository.findByStudentIsNull()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // READ — Chi tiết một Chapter (có kiểm tra quyền sở hữu)
    // ----------------------------------------------------------------

    /**
     * Xem chi tiết Chapter.
     * - Nếu là Chapter Hệ thống (student IS NULL): ai cũng xem được, không cần kiểm tra.
     * - Nếu là Chapter Cá nhân: phải đúng chủ sở hữu mới được xem.
     */
    @Transactional(readOnly = true)
    public VocabularyChapterResponse getChapterDetail(Long chapterId, Long studentId) {
        VocabularyChapter chapter = findChapterOrThrow(chapterId);

        boolean isPrivate = chapter.getStudent() != null;
        if (isPrivate) {
            verifyOwnership(chapter, studentId);
        }

        return toResponse(chapter);
    }

    // ----------------------------------------------------------------
    // UPDATE — chỉ chủ sở hữu được sửa
    // ----------------------------------------------------------------

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

        return toResponse(chapterRepository.save(chapter));
    }

    // ----------------------------------------------------------------
    // DELETE — chỉ chủ sở hữu được xóa
    // ----------------------------------------------------------------

    @Transactional
    public void deleteChapterForStudent(Long chapterId, Long studentId) {
        VocabularyChapter chapter = findChapterOrThrow(chapterId);
        verifyPrivateAndOwnership(chapter, studentId);
        chapterRepository.delete(chapter);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private VocabularyChapter findChapterOrThrow(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    /**
     * Kiểm tra Chapter phải là Private (student != null) VÀ đúng chủ sở hữu.
     * Dùng khi sửa/xóa — Student không được sửa/xóa Chapter Hệ thống.
     */
    private void verifyPrivateAndOwnership(VocabularyChapter chapter, Long studentId) {
        if (chapter.getStudent() == null) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
        verifyOwnership(chapter, studentId);
    }

    /**
     * Kiểm tra đúng chủ sở hữu (giả định chapter.student != null đã được kiểm tra trước).
     */
    private void verifyOwnership(VocabularyChapter chapter, Long studentId) {
        if (!chapter.getStudent().getStudentId().equals(studentId)) {
            throw new AppException(ErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }

    private VocabularyChapterResponse toResponse(VocabularyChapter chapter) {
        return VocabularyChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .studentId(chapter.getStudent() != null ? chapter.getStudent().getStudentId() : null)
                .chapterName(chapter.getChapterName())
                .level(chapter.getLevel())
                .orderIndex(chapter.getOrderIndex())
                .description(chapter.getDescription())
                .createdAt(chapter.getCreatedAt())
                .build();
    }
}
