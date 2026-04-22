package com.edumanager.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long receiverId);
    long countByReceiverIdAndIsReadFalse(Long receiverId);
    List<Notification> findTop5ByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
