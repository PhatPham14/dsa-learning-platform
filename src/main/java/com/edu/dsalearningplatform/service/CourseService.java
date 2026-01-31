package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.request.CreateCourseRequest;
import com.edu.dsalearningplatform.entity.Course;

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

     /**
      * Lấy thông tin khóa học theo ID.
      */
     Course getCourseById(Long courseId);

    /**
     * Thay đổi trạng thái active của khóa học (cho Admin).
     */
    void toggleStatus(Long courseId);
}
