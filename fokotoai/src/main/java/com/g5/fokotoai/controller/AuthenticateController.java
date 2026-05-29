package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.AuthenticateRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.AuthenticateResponse;
import com.g5.fokotoai.service.AuthenticateService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authen")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthenticateController {
    AuthenticateService authenticateService ;

    @PostMapping("/log-in")
    public ApiResponse<AuthenticateResponse> authenticated(@Valid @RequestBody AuthenticateRequest request){
        return ApiResponse.<AuthenticateResponse>builder()
                .code(8386)
                .message("success")
                .result(authenticateService.isAuthenticatedService(request))
                .build() ;
    }
}
