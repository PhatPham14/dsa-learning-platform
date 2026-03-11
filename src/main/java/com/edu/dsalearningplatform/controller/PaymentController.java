package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.dto.VnpayCallbackResult;
import com.edu.dsalearningplatform.entity.Payment;
import com.edu.dsalearningplatform.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/charge")
    @ResponseBody
    public ResponseEntity<?> charge(@RequestParam Integer studentId, @RequestParam Long courseId, @RequestParam(required = false) String provider) {
        Payment p = paymentService.charge(studentId, courseId, provider);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/vnpay/create")
    @ResponseBody
    public ResponseEntity<?> createVnpayPayment(@RequestParam Integer studentId,
                                                @RequestParam Long courseId,
                                                HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createVnpayPaymentUrl(studentId, courseId, getClientIp(request));
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/vnpay/return")
    public String vnpayReturn(@RequestParam Map<String, String> params) {
        VnpayCallbackResult result = paymentService.processVnpayCallback(params);
        Long courseId = result.courseId();

        if (result.success()) {
            return "redirect:/my-courses?paymentStatus=success";
        }

        StringBuilder redirect = new StringBuilder("redirect:/purchase?paymentStatus=failed");
        if (courseId != null) {
            redirect.append("&courseId=").append(courseId);
        }
        redirect.append("&paymentMessage=")
                .append(URLEncoder.encode(result.message(), StandardCharsets.UTF_8));
        return redirect.toString();
    }

    @GetMapping("/vnpay/ipn")
    @ResponseBody
    public ResponseEntity<?> vnpayIpn(@RequestParam Map<String, String> params) {
        VnpayCallbackResult result = paymentService.processVnpayCallback(params);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("RspCode", result.rspCode());
        response.put("Message", result.message());
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
