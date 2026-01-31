package com.edu.dsalearningplatform.dto.response;

public class LoginSuccessResponse {

    private String accessToken;
    private String refreshToken;
    private String type;
    private Integer userId;
    private String email;
    private String phone;
    private String fullName;
    private String userRole;

    public LoginSuccessResponse() {
    }

    public LoginSuccessResponse(String accessToken, String refreshToken, String type, Integer userId, String email, String phone, String fullName, String userRole) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.userRole = userRole;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
