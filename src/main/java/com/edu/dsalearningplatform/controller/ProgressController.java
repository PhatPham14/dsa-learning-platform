package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.ProgressEvent;
import com.edu.dsalearningplatform.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/record")
    public ResponseEntity<?> record(@RequestParam Integer studentId, @RequestParam Long courseId, @RequestParam String eventType, @RequestParam Integer value) {
        ProgressEvent ev = progressService.recordProgress(studentId, courseId, eventType, value);
        return ResponseEntity.ok(ev);
    }

    @GetMapping("/recommendation")
    public ResponseEntity<?> recommend(@RequestParam Integer studentId, @RequestParam Long courseId) {
        return ResponseEntity.ok(progressService.recommendNextTopic(studentId, courseId));
    }
}
