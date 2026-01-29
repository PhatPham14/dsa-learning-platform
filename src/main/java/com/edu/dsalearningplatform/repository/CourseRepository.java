package com.edu.dsalearningplatform.repository;

import com.edu.dsalearningplatform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Lấy một số khóa học đã publish mới nhất để hiển thị ở trang chủ.
     */
    List<Course> findTop6ByIsPublishedTrueOrderByCreatedAtDesc();
}
