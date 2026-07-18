package com.g5.fokotoai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.g5.fokotoai.dto.request.GeneratedQuestion;
import com.g5.fokotoai.dto.request.WeakVocabItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

/**
 * UC-20: Response trả về khi tạo thành công một Mini Challenge session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiMiniChallengeResponse {

    Long sessionId ;
    String studentLevel ;
    List<WeakVocabItem> weakVocabSnapshot ;
    List<GeneratedQuestion> generatedQuestions ;
    Instant createdAt ;
}
