package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.request.CreateCourseRequest;
import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest req) {
        Course saved = courseService.createCourse(req);
        return ResponseEntity.ok(saved);
    }
}
