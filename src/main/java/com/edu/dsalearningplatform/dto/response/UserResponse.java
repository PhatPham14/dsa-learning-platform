package com.edu.dsalearningplatform.dto.response;

import lombok.*;

@Data
public class UserResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phone;
    private String Role;
    private Boolean isActive;
    private String dateOfBirth;
    private String gender;
    private String address;
}

