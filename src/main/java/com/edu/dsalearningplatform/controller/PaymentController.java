package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.Payment;
import com.edu.dsalearningplatform.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestParam Integer studentId, @RequestParam Long courseId, @RequestParam(required = false) String provider) {
        Payment p = paymentService.charge(studentId, courseId, provider);
        return ResponseEntity.ok(p);
    }
}
