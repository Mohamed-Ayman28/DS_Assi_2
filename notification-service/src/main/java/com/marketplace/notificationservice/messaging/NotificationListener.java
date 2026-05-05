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

    /**
     * Admin PaymentFailed notifications — uses RabbitMQ DIRECT exchange
     * with routing key "PaymentFailed" (as required by the assignment).
     */
    @Transactional
    @RabbitListener(queues = AppConfig.ADMIN_PAYMENT_FAILED_Q)
    public void handleAdminPaymentFailed(Map<String, Object> message) {
        log.warn("[NotificationListener] ADMIN PaymentFailed event: {}", message);
        Notification notification = Notification.builder()
                .userId(0L)
                .username("ADMIN")
                .type("PAYMENT_FAILED")
                .message("Payment failed! BookingId=" + message.get("bookingId")
                        + ", Customer=" + message.get("customerName")
                        + ", Amount=$" + message.get("amount")
                        + ", Reason=" + message.get("reason"))
                .bookingId(message.get("bookingId") != null
                        ? Long.valueOf(message.get("bookingId").toString()) : null)
                .recipientType(Notification.RecipientType.ADMIN)
                .build();
        notificationRepository.save(notification);
    }

    private void saveNotification(Map<String, Object> message, Notification.RecipientType recipientType) {
        try {
            Notification notification = Notification.builder()
                    .userId(Long.valueOf(message.get("userId").toString()))
                    .username((String) message.get("username"))
                    .type((String) message.get("type"))
                    .message((String) message.get("message"))
                    .bookingId(message.get("bookingId") != null
                            ? Long.valueOf(message.get("bookingId").toString()) : null)
                    .recipientType(recipientType)
                    .build();
            notificationRepository.save(notification);
            log.info("[NotificationListener] Saved notification for userId={}", notification.getUserId());
        } catch (Exception e) {
            log.error("[NotificationListener] Failed to save notification: {}", e.getMessage(), e);
        }
    }
}
