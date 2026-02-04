package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.request.CreateCourseRequest;
import com.edu.dsalearningplatform.entity.*;

import java.util.List;

public interface CourseService {
    Course createCourse(CreateCourseRequest req);

    /**
     * Lấy danh sách khóa học nổi bật cho trang chủ (không yêu cầu đăng nhập).
     */
    List<Course> getFeaturedCourses();

    List<Course> getPendingCourses();

     /**
      * Lấy tất cả khóa học (có thể lọc/publish sau này) cho trang /courses.
      */
     List<Course> getAllCourses();
     
     List<Course> getCoursesByInstructor(Integer instructorId);

     /**
      * Lấy thông tin khóa học theo ID.
      */
     Course getCourseById(Long courseId);

    /**
     * Thay đổi trạng thái active của khóa học (cho Admin).
     */
    void toggleStatus(Long courseId);
    
    // Session management
    Session addSession(Long courseId, Session session);
    Session updateSession(Long sessionId, Session session);
    void deleteSession(Long sessionId);
    Session getSessionById(Long sessionId);
    
    // Video management
    Video addVideo(Long sessionId, Video video);
    Video updateVideo(Long videoId, Video video);
    void deleteVideo(Long videoId);
    Video getVideoById(Long videoId);
    
    // Quiz management
    Quiz addQuiz(Long sessionId, Quiz quiz);
    Quiz updateQuiz(Long quizId, Quiz quiz);
    void deleteQuiz(Long quizId);
    Quiz getQuizById(Long quizId);
    
    // Assignment management
    Assignment addAssignment(Long sessionId, Assignment assignment);
    Assignment updateAssignment(Long assignmentId, Assignment assignment);
    void deleteAssignment(Long assignmentId);
    Assignment getAssignmentById(Long assignmentId);
}
