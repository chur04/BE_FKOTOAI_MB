package com.g5.fokotoai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.fokotoai.dto.response.OcrResponse;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OcrService {

    RestClient restClient;
    ObjectProvider<StudentRepository> studentRepositoryProvider;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${ocr.api-url}")
    String ocrApiUrl;

    @NonFinal
    @Value("${translate.api-url}")
    String translateApiUrl;

    @NonFinal
    @Value("${app.student-validation.enabled:true}")
    boolean studentValidationEnabled;

    /**
     * OCR image → extract Japanese text → translate to Vietnamese.
     * Mimics the coldbrewga frontend flow: OCR first, then translate.
     */
    public OcrResponse recognize(Long studentId, MultipartFile image) {
        validateStudentExists(studentId);

        if (image == null || image.isEmpty()) {
            throw new AppException(ErrorCode.OCR_IMAGE_REQUIRED);
        }

        // Step 1: Call OCR API
        String originalText = callOcrApi(image);
        log.info("OCR extracted text for student_id={}: {}", studentId, originalText);

        // Step 2: Translate the extracted text (ja → vi)
        String translatedText = null;
        if (originalText != null && !originalText.isBlank()) {
            translatedText = callTranslateApi(originalText);
        }

        return OcrResponse.builder()
                .originalText(originalText)
                .translatedText(translatedText)
                .build();
    }

    private void validateStudentExists(Long studentId) {
        if (!studentValidationEnabled) {
            return;
        }

        StudentRepository repository = studentRepositoryProvider.getIfAvailable();
        if (repository == null || !repository.existsById(studentId)) {
            throw new AppException(ErrorCode.STUDENT_NOT_FOUND);
        }
    }

    private String callOcrApi(MultipartFile image) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            String rawResponse = restClient.post()
                    .uri(ocrApiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header("Accept", "*/*")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.debug("OCR API raw response: {}", rawResponse);

            Map<String, Object> map = objectMapper.readValue(
                    rawResponse, new TypeReference<Map<String, Object>>() {});

            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object text = dataMap.get("text");
                return text != null ? text.toString() : null;
            }

            return null;

        } catch (AppException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("OCR API timeout", e);
                throw new AppException(ErrorCode.OCR_API_TIMEOUT);
            }
            log.error("OCR API connection error", e);
            throw new AppException(ErrorCode.OCR_API_ERROR);
        } catch (Exception e) {
            log.error("OCR API unexpected error", e);
            throw new AppException(ErrorCode.OCR_API_ERROR);
        }
    }

    private String callTranslateApi(String text) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("text", text);
            body.put("sourceLang", "ja");
            body.put("targetLang", "vi");

            String rawResponse = restClient.post()
                    .uri(translateApiUrl)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.debug("Translate (OCR) API raw response: {}", rawResponse);

            Map<String, Object> map = objectMapper.readValue(
                    rawResponse, new TypeReference<Map<String, Object>>() {});

            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object translatedText = dataMap.get("translatedText");
                return translatedText != null ? translatedText.toString() : null;
            }

            Object translatedText = map.get("translatedText");
            return translatedText != null ? translatedText.toString() : null;

        } catch (Exception e) {
            log.error("Translate (OCR step) failed for text='{}'", text, e);
            return null; // OCR still succeeds even if translation fails
        }
    }
}
