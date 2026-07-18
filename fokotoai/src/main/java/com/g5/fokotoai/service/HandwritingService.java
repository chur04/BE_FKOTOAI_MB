package com.g5.fokotoai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.fokotoai.dto.request.HandwritingRecognitionRequest;
import com.g5.fokotoai.dto.response.HandwritingRecognitionResponse;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HandwritingService {

    RestClient restClient;
    ObjectProvider<StudentRepository> studentRepositoryProvider;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${handwriting.api-url}")
    String handwritingApiUrl;

    @NonFinal
    @Value("${handwriting.search-api-url}")
    String searchApiUrl;

    @NonFinal
    @Value("${app.student-validation.enabled:true}")
    boolean studentValidationEnabled;

    public HandwritingRecognitionResponse recognize(Long studentId, HandwritingRecognitionRequest request) {
        validateStudentExists(studentId);

        Map<String, Object> externalRequest = buildExternalRequest(request);
        log.debug("Handwriting external request body: {}", externalRequest);

        try {
            String rawResponse = restClient.post()
                    .uri(handwritingApiUrl)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .body(externalRequest)
                    .retrieve()
                    .body(String.class);

            log.info("Handwriting external API raw response: {}", rawResponse);

            List<String> candidates = parseCandidates(rawResponse);

            log.info("Handwriting recognized for student_id={}, candidates={}", studentId, candidates);

            return HandwritingRecognitionResponse.builder()
                    .candidates(candidates)
                    .build();

        } catch (AppException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Handwriting API timeout for student_id={}", studentId, e);
                throw new AppException(ErrorCode.HANDWRITING_API_TIMEOUT);
            }
            log.error("Handwriting API connection error for student_id={}", studentId, e);
            throw new AppException(ErrorCode.HANDWRITING_API_ERROR);
        } catch (Exception e) {
            log.error("Handwriting API unexpected error for student_id={}", studentId, e);
            throw new AppException(ErrorCode.HANDWRITING_API_ERROR);
        }
    }

    /**
     * Search dictionary/meaning for a confirmed word via external API.
     */
    public Map<String, Object> searchWord(Long studentId, String word) {
        validateStudentExists(studentId);

        Map<String, Object> searchRequest = new LinkedHashMap<>();
        searchRequest.put("keyword", word);

        try {
            String rawResponse = restClient.post()
                    .uri(searchApiUrl)
                    .header("Content-Type", "application/json")
                    .header("Accept", "*/*")
                    .body(searchRequest)
                    .retrieve()
                    .body(String.class);

            log.info("Handwriting search API raw response for word='{}': {}", word, rawResponse);

            Map<String, Object> result = objectMapper.readValue(
                    rawResponse, new TypeReference<Map<String, Object>>() {});
            return result;

        } catch (AppException e) {
            throw e;
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Handwriting search API timeout for word='{}'", word, e);
                throw new AppException(ErrorCode.HANDWRITING_API_TIMEOUT);
            }
            log.error("Handwriting search API connection error for word='{}'", word, e);
            throw new AppException(ErrorCode.HANDWRITING_API_ERROR);
        } catch (Exception e) {
            log.error("Handwriting search API unexpected error for word='{}'", word, e);
            throw new AppException(ErrorCode.HANDWRITING_API_ERROR);
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

    private Map<String, Object> buildExternalRequest(HandwritingRecognitionRequest request) {
        List<List<List<Double>>> ink = request.getInk().stream()
                .map(stroke -> List.of(stroke.getX(), stroke.getY(), stroke.getT()))
                .collect(Collectors.toList());

        Map<String, Object> writingGuide = new LinkedHashMap<>();
        writingGuide.put("writingAreaHeight", request.getWritingAreaHeight());
        writingGuide.put("writingAreaWidth", request.getWritingAreaWidth());

        Map<String, Object> innerRequest = new LinkedHashMap<>();
        innerRequest.put("maxCompletions", 0);
        innerRequest.put("maxNumResults", request.getMaxNumResults());
        innerRequest.put("preContext", request.getPreContext());
        innerRequest.put("writingGuide", writingGuide);
        innerRequest.put("ink", ink);

        Map<String, Object> externalRequest = new LinkedHashMap<>();
        externalRequest.put("apiLevel", "537.36");
        externalRequest.put("appVersion", 0.4);
        externalRequest.put("device", "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        externalRequest.put("inputType", 0);
        externalRequest.put("options", "enable_pre_space");
        externalRequest.put("requests", List.of(innerRequest));

        return externalRequest;
    }

    private List<String> parseCandidates(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return Collections.emptyList();
        }

        String trimmed = rawResponse.trim();

        try {
            if (trimmed.startsWith("{")) {
                Map<String, Object> map = objectMapper.readValue(
                        trimmed, new TypeReference<Map<String, Object>>() {});
                Object data = map.get("data");
                if (data instanceof List<?> dataList) {
                    return dataList.stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();
            }

            if (trimmed.startsWith("[")) {
                List<Object> list = objectMapper.readValue(
                        trimmed, new TypeReference<List<Object>>() {});
                if (list.isEmpty()) {
                    return Collections.emptyList();
                }

                Object first = list.get(0);
                if (first instanceof String) {
                    @SuppressWarnings("unchecked")
                    List<String> result = (List<String>) (List<?>) list;
                    return result;
                }
                if (first instanceof List<?> nestedList && !nestedList.isEmpty()
                        && nestedList.get(0) instanceof String) {
                    @SuppressWarnings("unchecked")
                    List<String> result = (List<String>) nestedList;
                    return result;
                }
            }

        } catch (Exception e) {
            log.warn("Failed to parse handwriting API response: {}", rawResponse, e);
        }

        log.warn("Unrecognized handwriting API response format: {}", rawResponse);
        return Collections.emptyList();
    }
}
