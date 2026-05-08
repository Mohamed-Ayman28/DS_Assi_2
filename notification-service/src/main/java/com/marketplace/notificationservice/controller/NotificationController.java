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

    @GetMapping("/api/notifications/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/api/notifications/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId));
    }

    @PatchMapping("/api/notifications/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationRepository.findById(id).map(n -> {
            n.setRead(true);
            return ResponseEntity.ok(notificationRepository.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/admin/notifications/payment-failed")
    public ResponseEntity<List<Notification>> getPaymentFailedNotifications() {
        return ResponseEntity.ok(
                notificationRepository.findByRecipientTypeOrderByCreatedAtDesc(Notification.RecipientType.ADMIN)
        );
    }

    @GetMapping("/api/admin/notifications")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
