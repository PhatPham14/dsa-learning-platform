package com.edu.dsalearningplatform.entity;

import com.edu.dsalearningplatform.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Nationalized
    @Column(name = "full_name", nullable = false, columnDefinition = "nvarchar(100)")
    private String fullName;

    @Nationalized
    @Column(name = "date_of_birth", columnDefinition = "nvarchar(20)")
    private String dateOfBirth;

    @Nationalized
    @Column(name = "gender", columnDefinition = "nvarchar(10)")
    private String gender;

    @Nationalized
    @Column(name = "address", columnDefinition = "nvarchar(255)")
    private String address;

    @Nationalized
    @Column(length = 100, unique = true, columnDefinition = "nvarchar(100)")
    private String email;

    @Nationalized
    @Column(nullable = false, length = 20, unique = true, columnDefinition = "nvarchar(20)")
    private String phone;

    @Nationalized
    @Column(name = "password_hash", nullable = false, length = 255, columnDefinition = "nvarchar(255)")
    @JsonIgnore // Che mật khẩu khi trả về API
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private boolean isActive = true;

    public User() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
