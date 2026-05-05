package com.marketplace.notificationservice.repository;

import com.marketplace.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByRecipientTypeOrderByCreatedAtDesc(Notification.RecipientType type);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
}
