package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> submit(@RequestParam Long courseId, @RequestParam Integer studentId, @RequestBody String code) {
        return ResponseEntity.ok(submissionService.submitCode(courseId, studentId, code));
    }
}
