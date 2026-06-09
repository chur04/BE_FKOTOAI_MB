package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.AddVocabToChapterRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.VocabularyChapterItemResponse;
import com.g5.fokotoai.service.VocabularyChapterItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vocabulary-chapters/{chapterId}/items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabularyChapterItemController {

    VocabularyChapterItemService chapterItemService;

    /**
     * Thêm từ vựng vào chapter (word + meaning).
     * POST /vocabulary-chapters/{chapterId}/items?studentId=xxx
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VocabularyChapterItemResponse> addVocabToChapter(
            @PathVariable Long chapterId,
            @RequestParam Long studentId,
            @Valid @RequestBody AddVocabToChapterRequest request) {

        return ApiResponse.<VocabularyChapterItemResponse>builder()
                .code(201)
                .message("Vocabulary added to chapter successfully")
                .result(chapterItemService.addVocabToChapter(chapterId, studentId, request))
                .build();
    }

    /**
     * Sửa (cập nhật) word và meaning của từ vựng trong chapter.
     * PUT /vocabulary-chapters/{chapterId}/items/{vocabId}?studentId=xxx
     */
    @PutMapping("/{vocabId}")
    public ApiResponse<VocabularyChapterItemResponse> updateVocabInChapter(
            @PathVariable Long chapterId,
            @PathVariable Long vocabId,
            @RequestParam Long studentId,
            @Valid @RequestBody AddVocabToChapterRequest request) {

        return ApiResponse.<VocabularyChapterItemResponse>builder()
                .code(200)
                .message("Vocabulary updated successfully")
                .result(chapterItemService.updateVocabInChapter(chapterId, vocabId, studentId, request))
                .build();
    }

    /**
     * Xóa (gỡ) từ vựng khỏi chapter.
     * DELETE /vocabulary-chapters/{chapterId}/items/{vocabId}?studentId=xxx
     */
    @DeleteMapping("/{vocabId}")
    public ApiResponse<Void> removeVocabFromChapter(
            @PathVariable Long chapterId,
            @PathVariable Long vocabId,
            @RequestParam Long studentId) {

        chapterItemService.removeVocabFromChapter(chapterId, vocabId, studentId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Vocabulary removed from chapter successfully")
                .build();
    }

    /**
     * Lấy danh sách từ vựng trong chapter.
     * GET /vocabulary-chapters/{chapterId}/items?studentId=xxx
     */
    @GetMapping
    public ApiResponse<List<VocabularyChapterItemResponse>> getVocabsInChapter(
            @PathVariable Long chapterId,
            @RequestParam Long studentId) {

        return ApiResponse.<List<VocabularyChapterItemResponse>>builder()
                .code(200)
                .message("Vocabularies in chapter retrieved successfully")
                .result(chapterItemService.getVocabsInChapter(chapterId, studentId))
                .build();
    }
}
