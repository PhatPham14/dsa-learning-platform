package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.repository.CourseRepository;
import com.edu.dsalearningplatform.repository.EnrollmentRepository;
import com.edu.dsalearningplatform.repository.PaymentRepository;
import com.edu.dsalearningplatform.repository.SubmissionRepository;
import com.edu.dsalearningplatform.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;
    private final SubmissionRepository submissionRepository;

    public DashboardServiceImpl(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository, PaymentRepository paymentRepository, SubmissionRepository submissionRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentRepository = paymentRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public Map<String, Object> getSummary() {
        Map<String, Object> out = new HashMap<>();
        out.put("totalCourses", courseRepository.count());
        out.put("totalEnrollments", enrollmentRepository.count());
        out.put("totalSubmissions", submissionRepository.count());
        BigDecimal totalRevenue = paymentRepository.findAll().stream().map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        out.put("totalRevenue", totalRevenue);
        return out;
    }
}
