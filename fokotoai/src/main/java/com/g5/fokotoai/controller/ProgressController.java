package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.response.*;
import com.g5.fokotoai.service.ProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UC-07: Track Learning Progress
 * Base URL: /api/v1/progress
 */
@RestController
@RequestMapping("v1/progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressController {

    ProgressService progressService;


    @GetMapping
    public ApiResponse<FlashCardProgressResponse> getMyProgress(
            @RequestParam Long studentId) {

        return ApiResponse.<FlashCardProgressResponse>builder()
                .code(200)
                .message("Learning progress retrieved successfully")
                .result(progressService.getStudentProgress(studentId))
                .build();
    }

    /**
     * GET /api/v1/progress/weak-vocab?studentId=1&limit=20
     * Trả về danh sách từ vựng yếu nhất của học sinh.
     *
     * @param studentId ID của học sinh
     * @param limit     số lượng từ muốn lấy (tuỳ chọn, mặc định 20)
     */
    @GetMapping("/weak-vocab")
    public ApiResponse<List<WeakVocabResponse>> getMyWeakVocabulary(
            @RequestParam Long studentId,
            @RequestParam(required = false) Integer limit) {

        return ApiResponse.<List<WeakVocabResponse>>builder()
                .code(200)
                .message("Weak vocabulary retrieved successfully")
                .result(progressService.getWeakVocabulary(studentId, limit))
                .build();
    }
}