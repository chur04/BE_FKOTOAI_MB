package com.g5.fokotoai.controller;


import com.g5.fokotoai.dto.request.StudentCreateRequest;
import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.dto.response.StudentResponse;
import com.g5.fokotoai.entity.Student;
import com.g5.fokotoai.service.StudentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)

public class StudentController {

    StudentService studentService ;

    @PostMapping
    public ApiResponse<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request){

        return ApiResponse.<StudentResponse>builder()
                .code(8386)
                .message("success")
                .result(studentService.createStudentService(request))
                .build() ;
    }

    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> getStudentById(@Valid @PathVariable("id") Long id){
        return ApiResponse.<StudentResponse>builder()
                .code(8386)
                .message("success")
                .result(studentService.getStudentByIdService(id))
                .build() ;
    }
}
