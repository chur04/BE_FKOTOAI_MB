package com.g5.fokotoai.mapper;

import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.dto.request.StudentAnswer;
import com.g5.fokotoai.dto.request.WeakVocabItem;
import com.g5.fokotoai.dto.response.AiMiniChallengeResponse;
import com.g5.fokotoai.dto.response.ChallengeReviewResponse;
import com.g5.fokotoai.dto.response.ChatResponse;
import com.g5.fokotoai.dto.response.QuestionReviewItem;
import com.g5.fokotoai.entity.AiAnalysisLog;
import com.g5.fokotoai.entity.AiMiniChallengeSession;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.entity.UserWordMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiChallengeMapper {

    @Mapping(target = "studentLevel", expression = "java(session.getStudent().getCurrentLevel() != null ? session.getStudent().getCurrentLevel().name() : null)")
    AiMiniChallengeResponse toAiMiniChallengeResponse(AiMiniChallengeSession session) ;

    @Mapping(target = "vocabId",       source = "vocab.vocabId")
    @Mapping(target = "word",          source = "vocab.word")
    @Mapping(target = "errorCount",    expression = "java(metric.getTotalWrongCount() != null ? metric.getTotalWrongCount() : 0)")
    @Mapping(target = "weaknessScore", expression = "java(metric.getTotalWrongCount() != null ? metric.getTotalWrongCount().doubleValue() : 0.0)")
    WeakVocabItem toWeakVocabItem(UserWordMetric metric) ;

    List<WeakVocabItem> toWeakVocabItemList(List<UserWordMetric> metrics) ;

    default QuestionReviewItem toQuestionReviewItem(int questionNumber, GeneratedQuestion question, String selectedOption, boolean isCorrect) {
        return QuestionReviewItem.builder()
                .questionNumber(questionNumber)
                .question(question.getQuestion())
                .options(question.getOptions())
                .yourAnswer(selectedOption)
                .correctAnswer(question.getCorrectAnswer())
                .isCorrect(isCorrect)
                .aiExplanation(null)
                .build() ;
    }

    @Mapping(target = "logId",      ignore = true)
    @Mapping(target = "article",    ignore = true)
    @Mapping(target = "createdAt",  expression = "java(java.time.Instant.now())")
    AiAnalysisLog toAiAnalysisLog(Student student, String selectedText, String aiResponse, Integer tokensUsed) ;

    @Mapping(target = "sessionId",  ignore = true)
    @Mapping(target = "attempt",    ignore = true)
    @Mapping(target = "tokensUsed", ignore = true)
    @Mapping(target = "createdAt",  expression = "java(java.time.Instant.now())")
    AiMiniChallengeSession toAiMiniChallengeSession(Student student, List<WeakVocabItem> weakVocabSnapshot, List<GeneratedQuestion> generatedQuestions) ;

    ChallengeReviewResponse toChallengeReviewResponse(Long sessionId, Integer totalQuestions, Integer correctCount, java.math.BigDecimal score, List<QuestionReviewItem> reviews) ;

    ChatResponse toChatResponse(String message, Integer tokensUsed) ;
}
