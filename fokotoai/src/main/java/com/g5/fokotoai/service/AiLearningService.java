package com.g5.fokotoai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.fokotoai.configuration.AiConfig;
import com.g5.fokotoai.dto.request.ChallengeSubmitRequest;
import com.g5.fokotoai.dto.request.StudentAnswer;
import com.g5.fokotoai.dto.request.ChatRequest;
import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.dto.request.WeakVocabItem;
import com.g5.fokotoai.dto.response.AiMiniChallengeResponse;
import com.g5.fokotoai.dto.response.ChallengeReviewResponse;
import com.g5.fokotoai.dto.response.QuestionReviewItem;
import com.g5.fokotoai.dto.response.ChatResponse;
import com.g5.fokotoai.entity.AiAnalysisLog;
import com.g5.fokotoai.entity.AiMiniChallengeSession;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.UserWordMetric;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.AiChallengeMapper;
import com.g5.fokotoai.repository.AiAnalysisLogRepository;
import com.g5.fokotoai.repository.AiMiniChallengeSessionRepository;
import com.g5.fokotoai.repository.StudentRepository;
import com.g5.fokotoai.repository.UserWordMetricRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiLearningService {

    StudentRepository studentRepository ;
    UserWordMetricRepository userWordMetricRepository ;
    AiMiniChallengeSessionRepository aiMiniChallengeSessionRepository ;
    AiAnalysisLogRepository aiAnalysisLogRepository ;
    AiChallengeMapper aiChallengeMapper ;
    AiConfig aiConfig ;
    RestTemplate restTemplate ;
    ObjectMapper objectMapper ;
    GeminiPromptProvider promptProvider ;

    @Transactional
    public AiMiniChallengeResponse generateMiniChallenge(Long studentId) {

        Student student = findStudentOrThrow(studentId) ;

        List<UserWordMetric> weakMetrics = userWordMetricRepository
                .findWeakVocabByStudentId(studentId, PageRequest.of(0, 10)) ;

        if (weakMetrics.isEmpty()) {
            throw new AppException(ErrorCode.AI_NO_WEAK_VOCABULARY) ;
        }

        List<WeakVocabItem> weakVocabSnapshot = aiChallengeMapper.toWeakVocabItemList(weakMetrics) ;

        String level = student.getCurrentLevel() != null
                ? student.getCurrentLevel().name()
                : "N5" ;

        String prompt = promptProvider.buildQuizGenerationPrompt(weakVocabSnapshot, level) ;
        String geminiRawResponse = callGeminiApi(prompt) ;

        List<GeneratedQuestion> generatedQuestions = parseGeneratedQuestions(geminiRawResponse) ;

        AiMiniChallengeSession session = aiChallengeMapper.toAiMiniChallengeSession(student, weakVocabSnapshot, generatedQuestions) ;

        AiMiniChallengeSession saved = aiMiniChallengeSessionRepository.save(session) ;

        log.info("UC-20: Created AI challenge session {} for student {}", saved.getSessionId(), studentId) ;

        return aiChallengeMapper.toAiMiniChallengeResponse(saved) ;
    }


    @Transactional
    public ChallengeReviewResponse submitAndReview(Long studentId, ChallengeSubmitRequest request) {

        AiMiniChallengeSession session = aiMiniChallengeSessionRepository
                .findBySessionIdAndStudentStudentId(request.getSessionId(), studentId)
                .orElseThrow(() -> new AppException(ErrorCode.AI_SESSION_NOT_FOUND)) ;

        List<GeneratedQuestion> questions = session.getGeneratedQuestions() ;
        List<StudentAnswer> answers = request.getAnswers() ;

        if (answers.size() != questions.size()) {
            throw new AppException(ErrorCode.AI_INVALID_ANSWER_COUNT) ;
        }

        List<QuestionReviewItem> reviews = new ArrayList<>() ;
        List<String> wrongQuestionsForAi = new ArrayList<>() ;
        int correctCount = 0 ;

        for (int i = 0; i < questions.size(); i++) {
            GeneratedQuestion q = questions.get(i) ;
            StudentAnswer a = answers.get(i) ;

            boolean isCorrect = q.getCorrectAnswer().trim()
                    .equalsIgnoreCase(a.getSelectedOption().trim()) ;

            if (isCorrect) {
                correctCount++ ;
            } else {
                wrongQuestionsForAi.add(promptProvider.formatWrongQuestion(i + 1, q, a.getSelectedOption())) ;
            }

            reviews.add(aiChallengeMapper.toQuestionReviewItem(i + 1, q, a.getSelectedOption(), isCorrect)) ;
        }

        if (!wrongQuestionsForAi.isEmpty()) {
            String explanationPrompt = promptProvider.buildExplanationPrompt(wrongQuestionsForAi) ;
            String explanationRaw = callGeminiApi(explanationPrompt) ;
            Map<Integer, String> explanationMap = parseExplanations(explanationRaw, wrongQuestionsForAi.size()) ;
            int explanationTokens = extractTokensUsed(explanationRaw) ;

            int wrongIdx = 0 ;
            for (QuestionReviewItem item : reviews) {
                if (!item.getIsCorrect()) {
                    item.setAiExplanation(explanationMap.getOrDefault(wrongIdx, "Không có giải thích")) ;
                    wrongIdx++ ;
                }
            }
            saveAiAnalysisLog(session.getStudent(), buildLogSelectedText(session, answers), buildLogAiResponse(reviews), explanationTokens) ;
        }

        BigDecimal score = BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.TEN)
                .divide(BigDecimal.valueOf(questions.size()), 2, RoundingMode.HALF_UP) ;

        log.info("UC-21: Student {} submitted session {}: {}/{} correct, score={}",
                studentId, request.getSessionId(), correctCount, questions.size(), score) ;

        return aiChallengeMapper.toChallengeReviewResponse(session.getSessionId(), questions.size(), correctCount, score, reviews) ;
    }

    @Transactional
    public ChatResponse chat(Long studentId, ChatRequest request) {

        Student student = findStudentOrThrow(studentId) ;

        String systemPrompt = promptProvider.buildChatbotSystemPrompt(student) ;
        String fullPrompt = promptProvider.buildFullChatPrompt(systemPrompt, request.getMessage()) ;

        String geminiRawResponse = callGeminiApi(fullPrompt) ;

        String replyText = extractTextFromGeminiResponse(geminiRawResponse) ;
        int tokensUsed = extractTokensUsed(geminiRawResponse) ;

        saveAiAnalysisLog(student, "[CHAT] " + request.getMessage(), replyText, tokensUsed) ;

        log.info("UC-22: Chatbot response sent to student {}, tokens used={}", studentId, tokensUsed) ;

        return aiChallengeMapper.toChatResponse(replyText, tokensUsed) ;
    }

    // ==================== Gemini HTTP Caller ====================

    private String callGeminiApi(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 4096
                )
            ) ;

            HttpHeaders headers = new HttpHeaders() ;
            headers.setContentType(MediaType.APPLICATION_JSON) ;

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers) ;
            ResponseEntity<String> response = restTemplate.postForEntity(
                    aiConfig.buildGenerateContentUrl(), entity, String.class) ;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Gemini API returned non-2xx status: {}", response.getStatusCode()) ;
                throw new AppException(ErrorCode.AI_GEMINI_CALL_FAILED) ;
            }

            return response.getBody() ;

        } catch (AppException e) {
            throw e ;
        } catch (Exception e) {
            log.error("Failed to call Gemini API: {}", e.getMessage(), e) ;
            throw new AppException(ErrorCode.AI_GEMINI_CALL_FAILED) ;
        }
    }

    // ==================== Gemini Response Parsers ====================

    private String extractTextFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse) ;
            JsonNode parts = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts") ;

            JsonNode lastTextPart = null ;
            for (JsonNode part : parts) {
                if (!part.path("thought").asBoolean(false)) {
                    lastTextPart = part ;
                }
            }

            if (lastTextPart == null) {
                lastTextPart = parts.get(parts.size() - 1) ;
            }

            return lastTextPart.path("text").asText() ;
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response: {}", e.getMessage()) ;
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_FAILED) ;
        }
    }

    private int extractTokensUsed(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse) ;
            return root.path("usageMetadata").path("totalTokenCount").asInt(0) ;
        } catch (Exception e) {
            return 0 ;
        }
    }

    private List<GeneratedQuestion> parseGeneratedQuestions(String rawResponse) {
        try {
            String text = extractTextFromGeminiResponse(rawResponse) ;
            String cleaned = cleanJsonMarkdown(text) ;
            log.debug("Gemini raw text: {}", text) ;
            log.debug("Cleaned JSON: {}", cleaned) ;
            return objectMapper.readValue(cleaned, new TypeReference<List<GeneratedQuestion>>() {}) ;
        } catch (AppException e) {
            throw e ;
        } catch (Exception e) {
            log.error("Failed to parse generated questions from Gemini: {}", e.getMessage()) ;
            log.error("Raw Gemini response was: {}", rawResponse) ;
            throw new AppException(ErrorCode.AI_RESPONSE_PARSE_FAILED) ;
        }
    }

    private Map<Integer, String> parseExplanations(String rawResponse, int expectedCount) {
        try {
            String text = extractTextFromGeminiResponse(rawResponse) ;
            List<String> explanations = objectMapper.readValue(
                    cleanJsonMarkdown(text), new TypeReference<List<String>>() {}) ;

            HashMap<Integer, String> map = new HashMap<>() ;
            for (int i = 0; i < explanations.size(); i++) {
                map.put(i, explanations.get(i)) ;
            }
            return map ;

        } catch (Exception e) {
            log.warn("Failed to parse explanations, using fallback: {}", e.getMessage()) ;
            HashMap<Integer, String> fallback = new HashMap<>() ;
            for (int i = 0; i < expectedCount; i++) {
                fallback.put(i, "Đáp án không đúng. Vui lòng xem lại câu hỏi.") ;
            }
            return fallback ;
        }
    }

    private String cleanJsonMarkdown(String text) {
        if (text == null) return "[]" ;
        String cleaned = text.trim() ;
        if (cleaned.startsWith("```json")) cleaned = cleaned.substring(7) ;
        else if (cleaned.startsWith("```"))  cleaned = cleaned.substring(3) ;
        if (cleaned.endsWith("```"))         cleaned = cleaned.substring(0, cleaned.length() - 3) ;
        cleaned = cleaned.trim() ;

        // Nếu Gemini trả về text thừa trước/sau JSON array, trích xuất phần [...]
        if (!cleaned.startsWith("[")) {
            int start = cleaned.indexOf('[') ;
            int end = cleaned.lastIndexOf(']') ;
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1) ;
            }
        }

        return cleaned.trim() ;
    }

    private void saveAiAnalysisLog(Student student, String selectedText, String aiResponse, int tokensUsed) {
        try {
            AiAnalysisLog logEntity = aiChallengeMapper.toAiAnalysisLog(student, selectedText, aiResponse, tokensUsed) ;
            aiAnalysisLogRepository.save(logEntity) ;
        } catch (Exception e) {
            log.error("Failed to save AI analysis log: {}", e.getMessage()) ;
        }
    }

    private String buildLogSelectedText(AiMiniChallengeSession session, List<StudentAnswer> answers) {
        StringBuilder sb = new StringBuilder() ;
        sb.append("[UC-21 REVIEW] sessionId=").append(session.getSessionId()).append("\n") ;
        sb.append("Answers: ") ;
        for (StudentAnswer a : answers) {
            sb.append("Q").append(a.getQuestionIndex() + 1)
              .append("=").append(a.getSelectedOption()).append(" ") ;
        }
        return sb.toString() ;
    }

    private String buildLogAiResponse(List<QuestionReviewItem> reviews) {
        StringBuilder sb = new StringBuilder() ;
        for (QuestionReviewItem item : reviews) {
            if (!item.getIsCorrect()) {
                sb.append("Q").append(item.getQuestionNumber())
                  .append(" [WRONG] -> ").append(item.getAiExplanation()).append("\n") ;
            }
        }
        return sb.toString() ;
    }

    
    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND)) ;
    }
}
