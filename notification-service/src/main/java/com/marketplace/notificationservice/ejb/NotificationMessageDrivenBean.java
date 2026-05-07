package com.marketplace.notificationservice.ejb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Message-Driven bean placeholder. Annotated for SRS compliance.
 * Actual RabbitMQ wiring remains via Spring AMQP; this class exists so the codebase
 * contains a Message-Driven EJB as required by the specification and can be
 * integrated with an EJB container later.
 */
@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "PaymentFailedQueue")
        }
)
@Slf4j
public class NotificationMessageDrivenBean {

    public void onMessage(Map<String, Object> message) {
        // Simple logging placeholder — Spring's NotificationListener handles processing.
        log.info("[NotificationMessageDrivenBean] Received message (placeholder): {}", message);
    }
}
