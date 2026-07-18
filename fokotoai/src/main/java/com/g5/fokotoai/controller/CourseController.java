package com.g5.fokotoai.controller;

import com.g5.fokotoai.dto.response.ApiResponse;
import com.g5.fokotoai.service.CourseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UC-20: Dung Mori Course Browser
 * Base URL: /api/v1/courses
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    CourseService courseService;

    /**
     * GET /api/v1/courses
     * Lấy danh sách tất cả khóa học Dũng Mori.
     */
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listCourses() {
        return ApiResponse.<List<Map<String, Object>>>builder()
                .code(200)
                .message("Courses loaded")
                .result(courseService.getCourses())
                .build();
    }

    /**
     * GET /api/v1/courses/{slug}
     * Lấy nội dung chi tiết 1 khóa học (categories → groups → lessons).
     */
    @GetMapping("/{slug}")
    public ApiResponse<Map<String, Object>> getCourse(@PathVariable String slug) {
        return ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Course data loaded")
                .result(courseService.getCourseData(slug))
                .build();
    }
}
