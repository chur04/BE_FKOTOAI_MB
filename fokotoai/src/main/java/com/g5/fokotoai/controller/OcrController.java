package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.OcrResponse;
import com.g5.fokotoai.service.OcrService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * UC-12: OCR Image Translation
 * Base URL: /api/v1/ocr
 */
@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OcrController {

    OcrService ocrService;

    /**
     * POST /api/v1/ocr/recognize?studentId=1
     * Nhận diện chữ Nhật từ ảnh và dịch sang tiếng Việt.
     *
     * Form-data: image = <file ảnh>
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrResponse> recognize(
            @RequestParam Long studentId,
            @RequestParam("image") MultipartFile image) {

        return ApiResponse.<OcrResponse>builder()
                .code(200)
                .message("OCR completed")
                .result(ocrService.recognize(studentId, image))
                .build();
    }
}
