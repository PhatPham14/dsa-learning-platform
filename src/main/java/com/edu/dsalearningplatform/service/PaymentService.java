package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.Payment;

public interface PaymentService {
    Payment charge(Integer studentId, Long courseId, String provider);
}
