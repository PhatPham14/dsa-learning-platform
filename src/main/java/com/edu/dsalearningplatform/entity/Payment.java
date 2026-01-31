package com.edu.dsalearningplatform.entity;

import com.edu.dsalearningplatform.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    private BigDecimal amount;

    @Nationalized
    @Column(columnDefinition = "nvarchar(100)")
    private String provider; // e.g., "mock_gateway"

    @Column(name = "admin_share")
    private BigDecimal adminShare;

    @Column(name = "instructor_share")
    private BigDecimal instructorShare;

    @CreationTimestamp
    private LocalDateTime paidAt;

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public BigDecimal getAdminShare() {
        return adminShare;
    }

    public void setAdminShare(BigDecimal adminShare) {
        this.adminShare = adminShare;
    }

    public BigDecimal getInstructorShare() {
        return instructorShare;
    }

    public void setInstructorShare(BigDecimal instructorShare) {
        this.instructorShare = instructorShare;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
