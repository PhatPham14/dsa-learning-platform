package com.edu.dsalearningplatform.entity;

import com.edu.dsalearningplatform.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
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


}