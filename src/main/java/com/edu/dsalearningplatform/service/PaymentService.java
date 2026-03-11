package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.InstructorPaymentDTO;
import com.edu.dsalearningplatform.dto.VnpayCallbackResult;
import com.edu.dsalearningplatform.entity.Payment;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    Payment charge(Integer studentId, Long courseId, String provider);
    String createVnpayPaymentUrl(Integer studentId, Long courseId, String ipAddress);
    VnpayCallbackResult processVnpayCallback(Map<String, String> vnpParams);
    List<InstructorPaymentDTO> getPaymentsByInstructor(Integer instructorId);
    List<InstructorPaymentDTO> getAllPayments();
    List<Object[]> getBestSellingCoursesThisMonth();
    List<Object[]> getRevenueLast3Months();
}
