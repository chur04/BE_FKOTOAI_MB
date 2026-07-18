package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.ChallengeSubmitRequest;
import com.g5.fokotoai.dto.request.ChatRequest;
import com.g5.fokotoai.dto.response.AiMiniChallengeResponse;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.ChallengeReviewResponse;
import com.g5.fokotoai.dto.response.ChatResponse;
import com.g5.fokotoai.service.AiLearningService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiLearningController {

    AiLearningService aiLearningService ;


    @PostMapping("/challenge/generate")
    public ApiResponse<AiMiniChallengeResponse> generateChallenge(
            @RequestHeader("X-Student-Id") Long studentId) {

        return ApiResponse.<AiMiniChallengeResponse>builder()
                .code(8386)
                .message("Mini challenge generated successfully")
                .result(aiLearningService.generateMiniChallenge(studentId))
                .build() ;
    }


    @PostMapping("/challenge/submit")
    public ApiResponse<ChallengeReviewResponse> submitChallenge(
            @RequestHeader("X-Student-Id") Long studentId,
            @Valid @RequestBody ChallengeSubmitRequest request) {

        return ApiResponse.<ChallengeReviewResponse>builder()
                .code(8386)
                .message("Challenge submitted and reviewed successfully")
                .result(aiLearningService.submitAndReview(studentId, request))
                .build() ;
    }


    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(
            @RequestHeader("X-Student-Id") Long studentId,
            @Valid @RequestBody ChatRequest request) {

        return ApiResponse.<ChatResponse>builder()
                .code(8386)
                .message("success")
                .result(aiLearningService.chat(studentId, request))
                .build() ;
    }
}
