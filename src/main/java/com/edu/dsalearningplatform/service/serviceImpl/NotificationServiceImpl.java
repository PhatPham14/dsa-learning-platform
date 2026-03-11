package com.edu.dsalearningplatform.service.serviceImpl;

import com.edu.dsalearningplatform.entity.Notification;
import com.edu.dsalearningplatform.entity.User;
import com.edu.dsalearningplatform.enums.NotificationType;
import com.edu.dsalearningplatform.repository.NotificationRepository;
import com.edu.dsalearningplatform.repository.UserRepository;
import com.edu.dsalearningplatform.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Notification sendNotification(Integer recipientId, NotificationType type,
                                         String title, String message, String detailMessage,
                                         Long relatedCourseId, Integer relatedUserId) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) return null;

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDetailMessage(detailMessage);
        notification.setRelatedCourseId(relatedCourseId);
        notification.setRelatedUserId(relatedUserId);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId, Integer userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipient().getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    public void markAllAsRead(Integer userId) {
        List<Notification> unread = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
