package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.TranslateRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.TranslateResponse;
import com.g5.fokotoai.service.TranslateService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

/**
 * UC-11: Text Translation
 * Base URL: /api/v1/translate
 */
@RestController
@RequestMapping("/api/v1/translate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranslateController {

    TranslateService translateService;

    /**
     * POST /api/v1/translate/text?studentId=1
     * Dịch văn bản giữa các ngôn ngữ.
     *
     * Body: { "text": "こんにちは", "sourceLang": "ja", "targetLang": "vi" }
     */
    @PostMapping("/text")
    public ApiResponse<TranslateResponse> translateText(
            @RequestParam Long studentId,
            @Valid @RequestBody TranslateRequest request) {

        return ApiResponse.<TranslateResponse>builder()
                .code(200)
                .message("Translation completed")
                .result(translateService.translate(studentId, request))
                .build();
    }
}
