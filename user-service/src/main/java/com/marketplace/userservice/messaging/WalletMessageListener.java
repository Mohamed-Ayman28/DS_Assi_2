package com.marketplace.userservice.messaging;

/**
 * EJB Message Driven Bean (MDB) Simulation
 *
 * In a Jakarta EE environment, this class would be annotated with:
 *   @MessageDriven(activationConfig = {
 *     @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
 *     @ActivationConfigProperty(propertyName = "destination", propertyValue = "wallet.deduct.queue")
 *   })
 *
 * Here we use Spring's @RabbitListener, which provides the same asynchronous
 * message-consumption contract as an MDB:
 *  - Listens to a queue and processes messages asynchronously
 *  - Stateless; each message is an independent invocation
 *  - Decoupled from the message producer
 *
 * This listener handles wallet deductions and refunds triggered by the booking service.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.userservice.config.RabbitMQConfig;
import com.marketplace.userservice.dto.UserDto;
import com.marketplace.userservice.ejb.UserManagementBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletMessageListener {

    private final UserManagementBean userManagementBean;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Simulated MDB: consumes wallet deduction requests from the booking service.
     * Replies with success/failure result to the booking service reply queue.
     */
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.WALLET_DEDUCT_QUEUE)
    public void handleWalletDeduct(Map<String, Object> message) {
        log.info("[MDB] Received wallet deduct request: {}", message);
        try {
            Long userId = Long.valueOf(message.get("userId").toString());
            BigDecimal amount = new BigDecimal(message.get("amount").toString());
            String correlationId = (String) message.get("correlationId");
            String replyQueue = (String) message.get("replyQueue");

            boolean success = userManagementBean.deductFunds(userId, amount);

            Map<String, Object> reply = Map.of(
                    "correlationId", correlationId,
                    "success", success,
                    "userId", userId,
                    "amount", amount
            );

            if (replyQueue != null) {
                rabbitTemplate.convertAndSend(replyQueue, reply);
            }
            log.info("[MDB] Wallet deduct result for userId={}: {}", userId, success);
        } catch (Exception e) {
            log.error("[MDB] Error processing wallet deduct: {}", e.getMessage(), e);
        }
    }

    /**
     * Simulated MDB: consumes wallet refund requests (booking rollback).
     */
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.WALLET_REFUND_QUEUE)
    public void handleWalletRefund(Map<String, Object> message) {
        log.info("[MDB] Received wallet refund request: {}", message);
        try {
            Long userId = Long.valueOf(message.get("userId").toString());
            BigDecimal amount = new BigDecimal(message.get("amount").toString());
            String reason = message.getOrDefault("reason", "Booking failed").toString();

            userManagementBean.refundFunds(userId, amount);
            log.info("[MDB] Refunded {} to userId={} — reason: {}", amount, userId, reason);
        } catch (Exception e) {
            log.error("[MDB] Error processing wallet refund: {}", e.getMessage(), e);
        }
    }
}
