package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
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
import java.util.List;

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
        if (enrollmentRepository.existsByStudentUserIdAndCourseCourseId(studentId, courseId)) {
            throw new RuntimeException("User already enrolled in this course");
        }

        Course course = courseRepository.findById(courseId).orElseThrow();
        
        if (!course.isActive()) {
            throw new RuntimeException("Cannot purchase an inactive course");
        }

        // Check if student is instructor
        if (course.getInstructor() != null && course.getInstructor().getUserId().equals(studentId)) {
            throw new RuntimeException("Instructor cannot purchase their own course");
        }
        
        User user = userRepository.findById(studentId).orElseThrow();
        // Check if user is Admin
        if (user.getRole() == com.edu.dsalearningplatform.enums.UserRole.ADMIN) {
            throw new RuntimeException("Admin cannot purchase courses");
        }

        Payment p = new Payment();
        p.setCourse(course);
        p.setStudent(user);
        BigDecimal amount = course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
        p.setAmount(amount);
        
        // Calculate shares: Admin 15%, Instructor 85%
        BigDecimal adminShare = amount.multiply(new BigDecimal("0.15"));
        BigDecimal instructorShare = amount.subtract(adminShare);
        
        p.setAdminShare(adminShare);
        p.setInstructorShare(instructorShare);
        
        p.setProvider(provider == null ? "mock_gateway" : provider);
        Payment saved = paymentRepository.save(p);

        // auto-enroll after successful payment
        Enrollment e = new Enrollment();
        e.setCourse(course);
        e.setStudent(user);
        enrollmentRepository.save(e);

        return saved;
    }

    @Override
    public List<InstructorPaymentDTO> getPaymentsByInstructor(Integer instructorId) {
        return paymentRepository.findByCourse_Instructor_UserId(instructorId);
    }

    @Override
    public List<InstructorPaymentDTO> getAllPayments() {
        return paymentRepository.findAllPaymentsForAdmin();
    }

    @Override
    public List<Object[]> getBestSellingCoursesThisMonth() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        java.time.LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        return paymentRepository.findBestSellingCourses(startOfMonth, endOfMonth);
    }

    @Override
    public List<Object[]> getRevenueLast3Months() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOf3MonthsAgo = now.minusMonths(2).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return paymentRepository.findMonthlyRevenue(startOf3MonthsAgo);
    }
}
