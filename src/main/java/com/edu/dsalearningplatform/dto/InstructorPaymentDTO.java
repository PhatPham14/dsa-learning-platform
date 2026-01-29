package com.edu.dsalearningplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InstructorPaymentDTO {
    private String courseTitle;
    private String studentName;
    private LocalDateTime paidAt;
    private BigDecimal amount;
    private BigDecimal instructorShare;
    private BigDecimal adminShare;
}
