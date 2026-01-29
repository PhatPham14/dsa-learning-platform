package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.Enrollment;

public interface EnrollmentService {
    Enrollment enroll(Long courseId, Integer studentId);
}
