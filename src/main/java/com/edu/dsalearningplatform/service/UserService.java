package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.dto.request.UpdateProfileRequest;
import com.edu.dsalearningplatform.dto.request.UserUpdateRequest;
import com.edu.dsalearningplatform.dto.response.UserResponse;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.enums.UserRole;



import java.util.List;
import java.util.Optional;


public interface UserService {
    User registerUser(String fullName, String email, String phone, String plainPassword, UserRole role);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByPhone(String phone);
    Optional<User> getUserById(Integer id);
    List<UserResponse> getAllUsers(String fullName, String email, String phone,
                                   String roleStr, Boolean isActive,
                                   int pageNum, int pageSize, String sortStr);
    void updateUserId(Integer id, UserUpdateRequest userUpdateRequest);
    void updateUserStatus(Integer id, boolean isActive);
    void changePassword(Integer id, String oldPassword, String newPassword);

    // lấy thông tin người dùng đang đăng nhập
    UserResponse getMyProfile(User loggedInUser);
//    UserResponse getUserByPhone(String phone);

    // Cập nhật thông tin hồ sơ của người dùng đang đăng nhập.
    UserResponse updateMyProfile(User loggedInUser, UpdateProfileRequest request);
}
