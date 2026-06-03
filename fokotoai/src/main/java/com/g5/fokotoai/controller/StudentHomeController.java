package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.StudentHomeResponse;
import com.g5.fokotoai.service.StudentHomeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class StudentHomeController {
    StudentHomeService studentHomeService ;

    @GetMapping("/{studentId}/home")
    public ApiResponse<StudentHomeResponse> getStudentHome(@PathVariable Long studentId){
        return ApiResponse.<StudentHomeResponse>builder()
                .code(8386)
                .message("success")
                .result(studentHomeService.getStudentHomeService(studentId))
                .build() ;
    }
}

