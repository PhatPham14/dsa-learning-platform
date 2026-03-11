package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.Enrollment;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.enums.NotificationType;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.EnrollmentRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.EnrollmentService;
import com.edu.dsalearningplatform.service.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository,
                                 UserRepository userRepository, @Lazy NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Enrollment enroll(Long courseId, Integer studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        User user = userRepository.findById(studentId).orElseThrow();
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(user);
        Enrollment saved = enrollmentRepository.save(e);

        // Thông báo cho INSTRUCTOR của khóa học
        if (course.getInstructor() != null) {
            notificationService.sendNotification(
                course.getInstructor().getUserId(),
                NotificationType.STUDENT_ENROLLED,
                "Học viên mới đăng ký",
                user.getFullName() + " vừa mua khóa học \"" + course.getTitle() + "\".",
                "Học viên " + user.getFullName() + " (" + user.getEmail() + ") vừa đăng ký vào khóa học \"" +
                course.getTitle() + "\".\nThời gian: " + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                course.getCourseId(),
                studentId
            );
        }

        return saved;
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
