package com.edu.dsalearningplatform.dto.request;

import java.math.BigDecimal;
import java.util.List;

public class CreateCourseRequest {
    public String title;
    public String description;
    public BigDecimal price;
    public Integer instructorId;
    public String imageBase64;
    public List<CreateSessionRequest> sessions;

    public static class CreateSessionRequest {
        public String title;
        public String description;
        public Integer sessionOrder;
        public List<CreateVideoRequest> videos;
        public List<CreateQuizRequest> quizzes;
        public List<CreateAssignmentRequest> assignments;
    }

    public static class CreateVideoRequest {
        public String title;
        public String videoUrl;
        public Integer duration;
    }

    public static class CreateQuizRequest {
        public String title;
        public String description;
    }

    public static class CreateAssignmentRequest {
        public String title;
        public String description;
    }
}
