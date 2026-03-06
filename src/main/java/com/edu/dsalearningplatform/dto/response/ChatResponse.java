package com.edu.dsalearningplatform.dto.response;

import java.util.List;

public class ChatResponse {
    
    private String message;
    private List<CourseSearchResult> courses;

    public ChatResponse() {
    }

    public ChatResponse(String message) {
        this.message = message;
    }

    public ChatResponse(String message, List<CourseSearchResult> courses) {
        this.message = message;
        this.courses = courses;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CourseSearchResult> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseSearchResult> courses) {
        this.courses = courses;
    }

    public static class CourseSearchResult {
        private Long courseId;
        private String title;
        private String description;
        private String price;
        private String instructor;

        public CourseSearchResult() {
        }

        public CourseSearchResult(Long courseId, String title, String description, String price, String instructor) {
            this.courseId = courseId;
            this.title = title;
            this.description = description;
            this.price = price;
            this.instructor = instructor;
        }

        // Getters and setters
        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getInstructor() {
            return instructor;
        }

        public void setInstructor(String instructor) {
            this.instructor = instructor;
        }
    }
}
