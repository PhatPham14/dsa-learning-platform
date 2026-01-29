package com.edu.dsalearningplatform.entity;

import com.edu.dsalearningplatform.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
}
