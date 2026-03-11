package com.edu.dsalearningplatform.repository;

import com.edu.dsalearningplatform.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Integer recipientId);

    long countByRecipientUserIdAndIsReadFalse(Integer recipientId);

    List<Notification> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer recipientId);
}
