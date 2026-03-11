package com.edu.dsalearningplatform.controller;

import com.edu.dsalearningplatform.entity.Notification;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.security.jwt.JwtUtils;
import com.edu.dsalearningplatform.service.NotificationService;
import com.edu.dsalearningplatform.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final JwtUtils jwtUtils;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public NotificationController(NotificationService notificationService,
                                  UserService userService,
                                  JwtUtils jwtUtils) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String phone = jwtUtils.getUsernameFromJwtToken(token);
            return userService.getUserByPhone(phone).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return ResponseEntity.status(401).build();

        List<Notification> notifications = notificationService.getNotificationsForUser(user.getUserId());
        List<Map<String, Object>> result = notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getNotificationId());
            map.put("type", n.getType().name());
            map.put("title", n.getTitle());
            map.put("message", n.getMessage());
            map.put("detailMessage", n.getDetailMessage());
            map.put("isRead", n.isRead());
            map.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().format(FORMATTER) : "");
            map.put("relatedCourseId", n.getRelatedCourseId());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return ResponseEntity.status(401).build();

        long count = notificationService.getUnreadCount(user.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return ResponseEntity.status(401).build();

        notificationService.markAsRead(id, user.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) return ResponseEntity.status(401).build();

        notificationService.markAllAsRead(user.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
