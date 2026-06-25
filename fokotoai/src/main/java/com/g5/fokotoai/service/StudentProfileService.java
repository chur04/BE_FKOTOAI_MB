package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.UpdateProfileRequest;
import com.g5.fokotoai.dto.response.StudentProfileResponse;
import com.g5.fokotoai.entity.PaymentTransaction;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.enums.TransactionStatus;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.StudentMapper;
import com.g5.fokotoai.repository.PaymentTransactionRepository;
import com.g5.fokotoai.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentProfileService {

    StudentRepository studentRepository ;
    PaymentTransactionRepository paymentTransactionRepository ;
    StudentMapper studentMapper ;

    // UC-View Profile
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(Long studentId) {
        Student student = findStudentOrThrow(studentId) ;

        StudentProfileResponse response = studentMapper.toProfileResponse(student) ;

        boolean hasPremium = isPremiumActive(student.getQuizSubscriptionExpiry()) ;
        response.setHasPremium(hasPremium) ;

        if (hasPremium) {
            Optional<PaymentTransaction> latestSuccessTx =
                    paymentTransactionRepository
                            .findTopByStudentStudentIdAndStatusOrderByCreatedAtDesc(
                                    studentId, TransactionStatus.SUCCESS) ;

            latestSuccessTx.ifPresent(tx ->
                    response.setCurrentPackageName(tx.getPackageField().getPackageName())
            ) ;
        }

        return response ;
    }

    // UC-Update Profile
    @Transactional
    public StudentProfileResponse updateProfile(Long studentId, UpdateProfileRequest request) {
        Student student = findStudentOrThrow(studentId) ;

        if (!student.getUsername().equals(request.getUsername())
                && studentRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED) ;
        }

        if (!student.getEmail().equals(request.getEmail())
                && studentRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED) ;
        }

        studentMapper.updateStudentFromRequest(request, student) ;

        Student saved = studentRepository.save(student) ;

        return getProfile(saved.getStudentId()) ;
    }

    // --- Helper ---

    private Student findStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND)) ;
    }

    private boolean isPremiumActive(Instant expiry) {
        return expiry != null && expiry.isAfter(Instant.now()) ;
    }
}
