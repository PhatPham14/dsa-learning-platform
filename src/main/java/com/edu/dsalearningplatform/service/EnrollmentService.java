package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.Enrollment;

import java.util.List;

public interface EnrollmentService {
    Enrollment enroll(Long courseId, Integer studentId);
    List<Enrollment> getEnrollmentsByStudent(Integer studentId);
    boolean isEnrolled(Integer studentId, Long courseId);
}
