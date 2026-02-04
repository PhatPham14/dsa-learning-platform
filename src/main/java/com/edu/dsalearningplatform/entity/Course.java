package com.edu.dsalearningplatform.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Nationalized
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Nationalized
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Nationalized
    @Column(name = "image_base64", columnDefinition = "nvarchar(max)")
    private String imageBase64;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Session> sessions = new java.util.ArrayList<>();

    public Course() {
    }

    public Long getCourseId() {
        return courseId;
    }


    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean isPublished() {
        return Boolean.TRUE.equals(isPublished);
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public void setActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public java.util.List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(java.util.List<Session> sessions) {
        this.sessions = sessions;
    }

    public String getDisplayImageUrl() {
        if (this.imageBase64 == null || this.imageBase64.trim().isEmpty()) {
            return "/images/default-course.svg";
        }
        String data = this.imageBase64.trim();
        // Check if it's a URL or path
        if (data.startsWith("http") || data.startsWith("/")) {
            return data;
        }
        // Check if it's already a Data URI
        if (data.startsWith("data:")) {
            return data;
        }
        // Assume raw base64 and add prefix
        return "data:image/jpeg;base64," + data;
    }
}
