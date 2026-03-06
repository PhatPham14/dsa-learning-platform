package com.edu.dsalearningplatform.service;

public interface AiService {
    int evaluateCode(String code);
    String recommendNextTopic(Integer studentId, Long courseId);
}
