package com.edu.dsalearningplatform.repository;

import com.edu.dsalearningplatform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentUserId(Integer studentId);
    boolean existsByStudentUserIdAndCourseCourseId(Integer studentId, Long courseId);
}
