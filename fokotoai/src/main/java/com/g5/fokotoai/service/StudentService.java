package com.g5.fokotoai.service;

import com.g5.fokotoai.dto.request.StudentCreateRequest;
import com.g5.fokotoai.dto.response.StudentResponse;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.exception.AppException;
import com.g5.fokotoai.exception.ErrorCode;
import com.g5.fokotoai.mapper.StudentMapper;
import com.g5.fokotoai.repository.StudentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class StudentService {

    StudentRepository studentRepository ;

    StudentMapper studentMapper ;

    public StudentResponse createStudentService(StudentCreateRequest request){
        if (studentRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.EMAIL_EXISTED) ;

        if (studentRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USERNAME_EXISTED);

        Student student = studentMapper.fromStudentCreateRequestToStudent(request) ;
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10) ;
        student.setPasswordHash(passwordEncoder.encode(student.getPasswordHash()));
        return studentMapper.fromStudentToStudentResponse(studentRepository.save(student)) ;
    }

    public StudentResponse getStudentByIdService(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND)) ;

        return studentMapper.fromStudentToStudentResponse(student) ;
    }

}
