package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.Enrollment;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.EnrollmentRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository, UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Enrollment enroll(Long courseId, Integer studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        User user = userRepository.findById(studentId).orElseThrow();
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(user);
        return enrollmentRepository.save(e);
    }

    @Override
    public java.util.List<Enrollment> getEnrollmentsByStudent(Integer studentId) {
        return enrollmentRepository.findByStudentUserId(studentId);
    }

    @Override
    public boolean isEnrolled(Integer studentId, Long courseId) {
        return enrollmentRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId);
    }
}
