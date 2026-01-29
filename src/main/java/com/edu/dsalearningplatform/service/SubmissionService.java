package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.Submission;

public interface SubmissionService {
    Submission submitCode(Long courseId, Integer studentId, String code);
}
