package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.HandwritingRecognitionRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.HandwritingRecognitionResponse;
import com.g5.fokotoai.service.HandwritingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * UC-10: Handwriting Recognition
 * Base URL: /api/v1/handwriting
 */
@RestController
@RequestMapping("/api/v1/handwriting")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HandwritingController {

    HandwritingService handwritingService;

    /**
     * POST /api/v1/handwriting/recognize?studentId=1
     * Nhan dien chu viet tay tu du lieu ink strokes.
     */
    @PostMapping("/recognize")
    public ApiResponse<HandwritingRecognitionResponse> recognize(
            @RequestParam Long studentId,
            @Valid @RequestBody HandwritingRecognitionRequest request) {

        return ApiResponse.<HandwritingRecognitionResponse>builder()
                .code(200)
                .message("Handwriting recognized successfully")
                .result(handwritingService.recognize(studentId, request))
                .build();
    }

    /**
     * POST /api/v1/handwriting/search?studentId=1&word=大
     * Tra cuu tu da chon qua external search API.
     */
    @PostMapping("/search")
    public ApiResponse<Map<String, Object>> searchWord(
            @RequestParam Long studentId,
            @RequestParam String word) {

        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Search completed")
                .result(handwritingService.searchWord(studentId, word))
                .build();
    }
}
