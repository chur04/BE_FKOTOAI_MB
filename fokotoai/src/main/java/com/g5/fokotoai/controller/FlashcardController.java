package com.g5.fokotoai.controller;


import com.g5.fokotoai.dto.request.FlashcardReviewRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.FlashcardResponse;
import com.g5.fokotoai.service.FlashcardService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UC-06: Study Flashcards
 * Base URL: /api/v1/flashcards
 */
@RestController
@RequestMapping("/api/v1/flashcards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashcardController {

    FlashcardService flashcardService;

    /**
     * GET /api/v1/flashcards/chapters/{chapterId}?studentId=1
     * Lấy danh sách thẻ học của một chapter kèm trạng thái học của student.
     */
    @GetMapping("/chapters/{chapterId}")
    public ApiResponse<List<FlashcardResponse>> getFlashcardsByChapter(
            @PathVariable Long chapterId,
            @RequestParam Long studentId) {

        return ApiResponse.<List<FlashcardResponse>>builder()
                .code(200)
                .message("Flashcards retrieved successfully")
                .result(flashcardService.getFlashcardsByChapter(studentId, chapterId))
                .build();
    }

    /**
     * POST /api/v1/flashcards/review?studentId=1
     * Học sinh nộp kết quả đánh giá "Đã thuộc / Chưa thuộc" cho một thẻ.
     */
    @PostMapping("/review")
    public ApiResponse<FlashcardResponse> submitReview(
            @RequestParam Long studentId,
            @Valid @RequestBody FlashcardReviewRequest request) {

        return ApiResponse.<FlashcardResponse>builder()
                .code(200)
                .message("Flashcard review submitted successfully")
                .result(flashcardService.submitFlashcardReview(studentId, request))
                .build();
    }
}