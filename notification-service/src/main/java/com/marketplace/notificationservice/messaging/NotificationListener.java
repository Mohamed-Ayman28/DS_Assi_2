package com.marketplace.notificationservice.messaging;

import com.marketplace.notificationservice.entity.Notification;
import com.marketplace.notificationservice.repository.NotificationRepository;
import com.marketplace.notificationservice.ejb.NotificationProcessorStateless;
import com.marketplace.notificationservice.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@Slf4j
public class NotificationListener {

    private final NotificationRepository notificationRepository;
    private final NotificationProcessorStateless notificationProcessorStateless;

    @Transactional
    @RabbitListener(queues = AppConfig.NOTIFICATION_CUSTOMER_Q)
    public void handleCustomerNotification(Map<String, Object> message) {
        log.info("[NotificationListener] Customer notification: {}", message);
        notificationProcessorStateless.processNotification(message, Notification.RecipientType.CUSTOMER);
    }

    @Transactional
    @RabbitListener(queues = AppConfig.NOTIFICATION_PROVIDER_Q)
    public void handleProviderNotification(Map<String, Object> message) {
        log.info("[NotificationListener] Provider notification: {}", message);
        notificationProcessorStateless.processNotification(message, Notification.RecipientType.PROVIDER);
    }

        @Transactional
        @RabbitListener(queues = AppConfig.ADMIN_PAYMENT_FAILED_Q)
        public void handleAdminPaymentFailed(Map<String, Object> message) {
        log.warn("[NotificationListener] ADMIN PaymentFailed event: {}", message);
        notificationProcessorStateless.processNotification(message, Notification.RecipientType.ADMIN);
        }

    private void saveNotification(Map<String, Object> message, Notification.RecipientType recipientType) {
        notificationProcessorStateless.processNotification(message, recipientType);
    }

    public NotificationListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationProcessorStateless = new NotificationProcessorStateless(notificationRepository);
    }
}
