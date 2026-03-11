package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.Course;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.dto.request.UserUpdateRequest;
import com.edu.dsalearningplatform.enums.NotificationType;
import com.edu.dsalearningplatform.enums.UserRole;
import com.edu.dsalearningplatform.service.CourseService;
import com.edu.dsalearningplatform.service.NotificationService;
import com.edu.dsalearningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private NotificationService notificationService;

    // User Management APIs
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        try {
            String fullName = request.get("fullName");
            String email = request.get("email");
            String phone = request.get("phone");
            String password = request.get("password");
            String roleStr = request.get("role");

            UserRole role = UserRole.valueOf(roleStr.toUpperCase());
            User newUser = userService.registerUser(fullName, email, phone, password, role);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Người dùng được tạo thành công");
            response.put("userId", newUser.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @RequestBody Map<String, String> request) {
        try {
            userService.getUserById(userId).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));

            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setFullName(request.get("fullName"));
            updateRequest.setEmail(request.get("email"));
            updateRequest.setPhone(request.get("phone"));
            userService.updateUserId(userId, updateRequest);

            if (request.get("role") != null) {
                userService.updateUserRole(userId, UserRole.valueOf(request.get("role").toUpperCase()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật người dùng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            userService.updateUserStatus(userId, false);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa người dùng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Course Management APIs
    @PutMapping("/courses/{courseId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCourse(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                throw new Exception("Không tìm thấy khóa học");
            }

            if (!course.isActive()) {
                courseService.toggleStatus(courseId);
            }

            // Thông báo cho INSTRUCTOR
            if (course.getInstructor() != null) {
                notificationService.sendNotification(
                    course.getInstructor().getUserId(),
                    NotificationType.COURSE_ENABLED,
                    "Khóa học đã được phê duyệt",
                    "Khóa học \"" + course.getTitle() + "\" đã được Admin phê duyệt và kích hoạt.",
                    "Admin đã phê duyệt và kích hoạt khóa học \"" + course.getTitle() + "\".\n" +
                    "Khóa học của bạn bây giờ đã sẵn sàng cho học viên đăng ký.",
                    courseId,
                    null
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xét duyệt khóa học thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/courses/{courseId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableCourse(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                throw new Exception("Không tìm thấy khóa học");
            }

            if (course.isActive()) {
                courseService.toggleStatus(courseId);
            }

            // Thông báo cho INSTRUCTOR
            if (course.getInstructor() != null) {
                notificationService.sendNotification(
                    course.getInstructor().getUserId(),
                    NotificationType.COURSE_DISABLED,
                    "Khóa học bị tạm ngưng",
                    "Khóa học \"" + course.getTitle() + "\" đã bị Admin tạm ngưng hoạt động.",
                    "Admin đã tạm ngưng khóa học \"" + course.getTitle() + "\".\n" +
                    "Khóa học hiện không còn hiển thị cho học viên. Liên hệ Admin nếu cần thêm thông tin.",
                    courseId,
                    null
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vô hiệu hóa khóa học thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/courses/{courseId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableCourse(@PathVariable Long courseId) {
        try {
            Course course = courseService.getCourseById(courseId);
            if (course == null) {
                throw new Exception("Không tìm thấy khóa học");
            }

            if (!course.isActive()) {
                courseService.toggleStatus(courseId);
            }

            // Thông báo cho INSTRUCTOR
            if (course.getInstructor() != null) {
                notificationService.sendNotification(
                    course.getInstructor().getUserId(),
                    NotificationType.COURSE_ENABLED,
                    "Khóa học đã được kích hoạt",
                    "Khóa học \"" + course.getTitle() + "\" đã được Admin kích hoạt.",
                    "Admin đã kích hoạt khóa học \"" + course.getTitle() + "\".\n" +
                    "Khóa học của bạn đã được hiện trên trang khóa học và học viên có thể đăng ký.",
                    courseId,
                    null
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Kích hoạt khóa học thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
