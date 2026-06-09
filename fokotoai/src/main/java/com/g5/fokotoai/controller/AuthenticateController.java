package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.request.*;
import com.g5.fokotoai.dto.response.*;
import com.g5.fokotoai.service.AuthenticateService;
import com.g5.fokotoai.service.PasswordResetService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/authen")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthenticateController {
    AuthenticateService authenticateService ;
    PasswordResetService passwordResetService ;

    @PostMapping("/log-in")
    public ApiResponse<AuthenticateResponse> authenticated(@Valid @RequestBody AuthenticateRequest request){
        return ApiResponse.<AuthenticateResponse>builder()
                .code(8386)
                .message("success")
                .result(authenticateService.isAuthenticatedService(request))
                .build() ;
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> isAuthenticatedForToken(@Valid @RequestBody IntrospectRequest request) throws ParseException , JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .code(8386)
                .message("success")
                .result(authenticateService.introspect(request))
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.<ForgotPasswordResponse>builder()
                .code(8386)
                .message("Success")
                .result(passwordResetService.sendOtp(request))
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ApiResponse.<VerifyOtpResponse>builder()
                .code(8386)
                .message("Success")
                .result(passwordResetService.verifyOtp(request))
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.<ResetPasswordResponse>builder()
                .code(8386)
                .message("Success")
                .result(passwordResetService.resetPassword(request))
                .build();
    }


    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@RequestBody LogoutRequest request) throws ParseException {
        return ApiResponse.<LogoutResponse>builder()
                .code(8386)
                .message("success")
                .result(authenticateService.logOutService(request))
                .build() ;
    }
}
