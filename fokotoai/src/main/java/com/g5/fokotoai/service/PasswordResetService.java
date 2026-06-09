package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.ForgotPasswordRequest;
import com.g5.fokotoai.dto.request.ResetPasswordRequest;
import com.g5.fokotoai.dto.request.VerifyOtpRequest;
import com.g5.fokotoai.dto.response.ForgotPasswordResponse;
import com.g5.fokotoai.dto.response.ResetPasswordResponse;
import com.g5.fokotoai.dto.response.VerifyOtpResponse;
import com.g5.fokotoai.entity.PasswordResetToken;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.repository.PasswordResetTokenRepository;
import com.g5.fokotoai.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class PasswordResetService {

    StudentRepository studentRepository ;
    PasswordResetTokenRepository passwordResetTokenRepository ;
    EmailService emailService ;

    Map<String, ResetTokenEntry> resetTokenCache = new ConcurrentHashMap<>();

    @Transactional
    public ForgotPasswordResponse sendOtp(ForgotPasswordRequest request){
        studentRepository.findByEmail(request.getEmail()).ifPresent(student -> {

            passwordResetTokenRepository.deleteByStudentAndIsUsedFalse(student);


            String otp = generateOtp() ;

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .student(student)
                    .token(otp)
                    .expiresAt(Instant.now().plus(10 , ChronoUnit.MINUTES))
                    .isUsed(false)
                    .build() ;

            passwordResetTokenRepository.save(passwordResetToken) ;

            emailService.sendOtpEmail(student.getEmail() , otp);


        });
        return ForgotPasswordResponse.builder()
                .message("OTP send successfully")
                .build();
    }
    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        Student student = studentRepository.findByEmail(request.getEmail()).orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        PasswordResetToken token = passwordResetTokenRepository.findByStudentAndTokenAndIsUsedFalse(student, request.getOtp()).orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        token.setIsUsed(true);
        passwordResetTokenRepository.save(token);

        String resetToken = UUID.randomUUID().toString();

        resetTokenCache.put(resetToken, new ResetTokenEntry(
                student.getEmail(),
                Instant.now().plusSeconds(300)
        ));

        log.info("OTP verified for student_id: {}", student.getStudentId());

        return VerifyOtpResponse.builder()
                .resetToken(resetToken)
                .message("Verify OTP successfully")
                .build();
    }
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }


        ResetTokenEntry entry = resetTokenCache.get(request.getResetToken());
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            resetTokenCache.remove(request.getResetToken());
            throw new AppException(ErrorCode.INVALID_RESET_TOKEN);
        }


        Student student = studentRepository.findByEmail(entry.email()).orElseThrow(() -> new AppException(ErrorCode.INVALID_RESET_TOKEN));


        student.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        studentRepository.save(student);


        resetTokenCache.remove(request.getResetToken());

        log.info("Password reset successfully for student_id: {}", student.getStudentId());

        return ResetPasswordResponse.builder()
                .message("Reset password successfully")
                .build();
    }



    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    record ResetTokenEntry(String email, Instant expiresAt) {}
}
