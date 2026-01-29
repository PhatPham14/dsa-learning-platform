package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.request.PurchaseRequest;
import com.edu.dsalearningplatform.entity.Enrollment;
import com.edu.dsalearningplatform.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest req) {
        // Payment flow is stubbed; directly enroll after "payment"
        Enrollment e = enrollmentService.enroll(req.courseId, req.studentId);
        return ResponseEntity.ok(e);
    }
}
