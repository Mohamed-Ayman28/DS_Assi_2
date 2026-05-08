package com.marketplace.notificationservice.ejb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "PaymentFailedQueue")
        }
)
@Slf4j
public class NotificationMessageDrivenBean {

    public void onMessage(Map<String, Object> message) {
        log.info("[NotificationMessageDrivenBean] Received message (placeholder): {}", message);
    }
}
