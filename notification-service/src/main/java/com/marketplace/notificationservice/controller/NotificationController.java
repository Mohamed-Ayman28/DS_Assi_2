package com.marketplace.notificationservice.controller;

import com.marketplace.notificationservice.entity.Notification;
import com.marketplace.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /** Get all notifications for a user (customer or provider) */
    @GetMapping("/api/notifications/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    /** Get only unread notifications */
    @GetMapping("/api/notifications/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId));
    }

    /** Mark a notification as read */
    @PatchMapping("/api/notifications/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setRead(true);
            return ResponseEntity.ok(notificationRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Admin: get all PaymentFailed notifications (via direct exchange) */
    @GetMapping("/api/admin/notifications/payment-failed")
    public ResponseEntity<List<Notification>> getPaymentFailedNotifications() {
        return ResponseEntity.ok(
                notificationRepository.findByRecipientTypeOrderByCreatedAtDesc(Notification.RecipientType.ADMIN)
        );
    }

    /** Admin: get all notifications */
    @GetMapping("/api/admin/notifications")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
