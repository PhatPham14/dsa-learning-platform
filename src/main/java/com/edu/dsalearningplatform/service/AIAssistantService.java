package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.response.ChatResponse;

public interface AIAssistantService {
    ChatResponse searchCourses(String userMessage);
}
