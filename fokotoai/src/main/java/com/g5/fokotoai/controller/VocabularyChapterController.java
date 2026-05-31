package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.VocabularyChapterRequest;
import com.g5.fokotoai.dto.response.ContinueChapterResponse;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.VocabularyChapterResponse;
import com.g5.fokotoai.service.VocabularyChapterService;
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
@RequestMapping("/api/v1/vocabulary-chapters")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VocabularyChapterController {

    VocabularyChapterService chapterService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VocabularyChapterResponse> createChapter(
            @RequestParam Long studentId,
            @Valid @RequestBody VocabularyChapterRequest request) {

        return ApiResponse.<VocabularyChapterResponse>builder()
                .code(201)
                .message("Chapter created successfully")
                .result(chapterService.createChapterForStudent(studentId, request))
                .build();
    }


    @GetMapping("/my")
    public ApiResponse<List<VocabularyChapterResponse>> getMyChapters(
            @RequestParam Long studentId) {

        return ApiResponse.<List<VocabularyChapterResponse>>builder()
                .code(200)
                .message("My chapters retrieved successfully")
                .result(chapterService.getMyChapters(studentId))
                .build();
    }


    @GetMapping("/system")
    public ApiResponse<List<VocabularyChapterResponse>> getSystemChapters() {

        return ApiResponse.<List<VocabularyChapterResponse>>builder()
                .code(200)
                .message("System chapters retrieved successfully")
                .result(chapterService.getSystemChapters())
                .build();
    }


    @GetMapping("/{chapterId}")
    public ApiResponse<VocabularyChapterResponse> getChapterDetail(
            @PathVariable Long chapterId,
            @RequestParam Long studentId) {

        return ApiResponse.<VocabularyChapterResponse>builder()
                .code(200)
                .message("Chapter retrieved successfully")
                .result(chapterService.getChapterDetail(chapterId, studentId))
                .build();
    }


    @PutMapping("/{chapterId}")
    public ApiResponse<VocabularyChapterResponse> updateChapter(
            @PathVariable Long chapterId,
            @RequestParam Long studentId,
            @Valid @RequestBody VocabularyChapterRequest request) {

        return ApiResponse.<VocabularyChapterResponse>builder()
                .code(200)
                .message("Chapter updated successfully")
                .result(chapterService.updateChapterForStudent(chapterId, studentId, request))
                .build();
    }


    @DeleteMapping("/{chapterId}")
    public ApiResponse<Void> deleteChapter(
            @PathVariable Long chapterId,
            @RequestParam Long studentId) {

        chapterService.deleteChapterForStudent(chapterId, studentId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Chapter deleted successfully")
                .build();
    }
}
