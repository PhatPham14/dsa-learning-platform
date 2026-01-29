package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.ProgressEvent;

public interface ProgressService {
    ProgressEvent recordProgress(Integer studentId, Long courseId, String eventType, Integer value);
    String recommendNextTopic(Integer studentId, Long courseId);
}
