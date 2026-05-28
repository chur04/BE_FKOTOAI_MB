package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.StudentHomeResponse;
import com.g5.fokotoai.service.StudentHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentHomeController {

    private final StudentHomeService studentHomeService;

    @GetMapping("/{studentId}/home")
    public ApiResponse<StudentHomeResponse> getStudentHome(@PathVariable Long studentId) {
        StudentHomeResponse homeData = studentHomeService.getStudentHomeData(studentId);
        return ApiResponse.<StudentHomeResponse>builder()
                .code(200)
                .message("Successfully retrieved student home dashboard data")
                .result(homeData)
                .build();
    }
}
