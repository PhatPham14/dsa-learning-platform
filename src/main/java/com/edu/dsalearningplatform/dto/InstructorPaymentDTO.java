package com.edu.dsalearningplatform.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InstructorPaymentDTO {
    private String courseTitle;
    private String studentName;
    private LocalDateTime paidAt;
    private BigDecimal amount;
    private BigDecimal instructorShare;
    private BigDecimal adminShare;

    public InstructorPaymentDTO() {
    }

    public InstructorPaymentDTO(String courseTitle, String studentName, LocalDateTime paidAt, BigDecimal amount, BigDecimal instructorShare, BigDecimal adminShare) {
        this.courseTitle = courseTitle;
        this.studentName = studentName;
        this.paidAt = paidAt;
        this.amount = amount;
        this.instructorShare = instructorShare;
        this.adminShare = adminShare;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getInstructorShare() {
        return instructorShare;
    }

    public void setInstructorShare(BigDecimal instructorShare) {
        this.instructorShare = instructorShare;
    }

    public BigDecimal getAdminShare() {
        return adminShare;
    }

    public void setAdminShare(BigDecimal adminShare) {
        this.adminShare = adminShare;
    }
}
