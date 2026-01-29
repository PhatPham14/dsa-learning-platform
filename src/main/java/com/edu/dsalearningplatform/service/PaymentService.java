package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
import com.edu.dsalearningplatform.entity.Payment;

import java.util.List;

public interface PaymentService {
    Payment charge(Integer studentId, Long courseId, String provider);
    List<InstructorPaymentDTO> getPaymentsByInstructor(Integer instructorId);
    List<InstructorPaymentDTO> getAllPayments();
    List<Object[]> getBestSellingCoursesThisMonth();
    List<Object[]> getRevenueLast3Months();
}
