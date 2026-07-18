package com.g5.fokotoai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.fokotoai.dto.request.TranslateRequest;
import com.g5.fokotoai.dto.response.TranslateResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranslateService {

    RestClient restClient;
    ObjectProvider<StudentRepository> studentRepositoryProvider;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${translate.api-url}")
    String translateApiUrl;

    @NonFinal
    @Value("${app.student-validation.enabled:true}")
    boolean studentValidationEnabled;

    /**
     * Call external coldbrewga translate API.
     */
    public TranslateResponse translate(Long studentId, TranslateRequest request) {
        validateStudentExists(studentId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("text", request.getText());
        body.put("sourceLang", request.getSourceLang());
        body.put("targetLang", request.getTargetLang());

        log.debug("Translate request body: {}", body);

        try {
            String rawResponse = restClient.post()
                    .uri(translateApiUrl)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("Translate API raw response: {}", rawResponse);

            Map<String, Object> map = objectMapper.readValue(
                    rawResponse, new TypeReference<Map<String, Object>>() {});

            Object data = map.get("data");
            String translatedText = null;
            String romaji = null;

            if (data instanceof Map<?, ?> dataMap) {
                translatedText = dataMap.get("translatedText") != null
                        ? dataMap.get("translatedText").toString() : null;
                romaji = dataMap.get("romaji") != null
                        ? dataMap.get("romaji").toString() : null;
            }

            // Fallback: response might have translatedText at root level
            if (translatedText == null) {
                translatedText = map.get("translatedText") != null
                        ? map.get("translatedText").toString() : null;
            }

            return TranslateResponse.builder()
                    .translatedText(translatedText)
                    .romaji(romaji)
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Translate API timeout for student_id={}", studentId, e);
                throw new AppException(ErrorCode.TRANSLATE_API_TIMEOUT);
            }
            log.error("Translate API connection error for student_id={}", studentId, e);
            throw new AppException(ErrorCode.TRANSLATE_API_ERROR);
        } catch (Exception e) {
            log.error("Translate API unexpected error for student_id={}", studentId, e);
            throw new AppException(ErrorCode.TRANSLATE_API_ERROR);
        }
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
}
