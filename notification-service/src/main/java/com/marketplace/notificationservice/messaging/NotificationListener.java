package com.marketplace.notificationservice.messaging;

import com.marketplace.notificationservice.entity.Notification;
import com.marketplace.notificationservice.repository.NotificationRepository;
import com.marketplace.notificationservice.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepository;

    @Transactional
    @RabbitListener(queues = AppConfig.NOTIFICATION_CUSTOMER_Q)
    public void handleCustomerNotification(Map<String, Object> message) {
        log.info("[NotificationListener] Customer notification: {}", message);
        saveNotification(message, Notification.RecipientType.CUSTOMER);
    }

    @Transactional
    @RabbitListener(queues = AppConfig.NOTIFICATION_PROVIDER_Q)
    public void handleProviderNotification(Map<String, Object> message) {
        log.info("[NotificationListener] Provider notification: {}", message);
        saveNotification(message, Notification.RecipientType.PROVIDER);
    }

    @Transactional
    @RabbitListener(queues = AppConfig.ADMIN_PAYMENT_FAILED_Q)
    public void handleAdminPaymentFailed(Map<String, Object> message) {
        log.warn("[NotificationListener] ADMIN PaymentFailed event: {}", message);
        saveNotification(message, Notification.RecipientType.ADMIN);
    }

    private void saveNotification(Map<String, Object> message, Notification.RecipientType recipientType) {
        try {
            Notification notification = Notification.builder()
                    .userId(message.get("userId") != null ? Long.valueOf(message.get("userId").toString()) : 0L)
                    .username(message.get("username") != null ? message.get("username").toString() : "")
                    .type(message.get("type") != null ? message.get("type").toString() : "")
                    .message(message.get("message") != null ? message.get("message").toString() : "")
                    .bookingId(message.get("bookingId") != null ? Long.valueOf(message.get("bookingId").toString()) : null)
                    .recipientType(recipientType)
                    .build();

            notificationRepository.save(Objects.requireNonNull(notification));
            log.info("[NotificationListener] Saved notification for userId={}", notification.getUserId());
        } catch (Exception e) {
            log.error("[NotificationListener] Failed to process notification: {}", e.getMessage(), e);
        }
    }
}
