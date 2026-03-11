package com.edu.dsalearningplatform.service;

import com.edu.dsalearningplatform.entity.Notification;
import com.edu.dsalearningplatform.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    /**
     * Gửi thông báo đến một người dùng cụ thể.
     */
    Notification sendNotification(Integer recipientId, NotificationType type,
                                  String title, String message, String detailMessage,
                                  Long relatedCourseId, Integer relatedUserId);

    /**
     * Lấy danh sách tất cả thông báo của người dùng (sắp xếp mới nhất trước).
     */
    List<Notification> getNotificationsForUser(Integer userId);

    /**
     * Lấy số lượng thông báo chưa đọc.
     */
    long getUnreadCount(Integer userId);

    /**
     * Đánh dấu thông báo là đã đọc.
     */
    void markAsRead(Long notificationId, Integer userId);

    /**
     * Đánh dấu tất cả thông báo của người dùng là đã đọc.
     */
    void markAllAsRead(Integer userId);
}
