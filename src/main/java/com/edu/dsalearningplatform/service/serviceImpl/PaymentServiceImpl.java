package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.Enrollment;
import com.edu.dsalearningplatform.entity.Payment;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.EnrollmentRepository;
import com.edu.dsalearningplatform.repository.PaymentRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, CourseRepository courseRepository, UserRepository userRepository, EnrollmentRepository enrollmentRepository) {
        this.paymentRepository = paymentRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Payment charge(Integer studentId, Long courseId, String provider) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        User user = userRepository.findById(studentId).orElseThrow();
        Payment p = new Payment();
        p.setCourse(course);
        p.setStudent(user);
        p.setAmount(course.getPrice() == null ? BigDecimal.ZERO : course.getPrice());
        p.setProvider(provider == null ? "mock_gateway" : provider);
        Payment saved = paymentRepository.save(p);

        // auto-enroll after successful payment
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(user);
        enrollmentRepository.save(e);

        return saved;
    }
}
