package com.marketplace.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange Names ────────────────────────────────────────────────────────
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String PAYMENTS_EXCHANGE = "payments.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // ── Queue Names ───────────────────────────────────────────────────────────
    public static final String WALLET_DEDUCT_QUEUE = "wallet.deduct.queue";
    public static final String WALLET_REFUND_QUEUE = "wallet.refund.queue";

    // ── Routing Keys ──────────────────────────────────────────────────────────
    public static final String PAYMENT_FAILED_KEY = "PaymentFailed";

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(BOOKING_EXCHANGE);
    }

    @Bean
    public DirectExchange paymentsExchange() {
        return new DirectExchange(PAYMENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue walletDeductQueue() {
        return QueueBuilder.durable(WALLET_DEDUCT_QUEUE).build();
    }

    @Bean
    public Queue walletRefundQueue() {
        return QueueBuilder.durable(WALLET_REFUND_QUEUE).build();
    }

    @Bean
    public Binding walletDeductBinding() {
        return BindingBuilder.bind(walletDeductQueue()).to(bookingExchange()).with("wallet.deduct");
    }

    @Bean
    public Binding walletRefundBinding() {
        return BindingBuilder.bind(walletRefundQueue()).to(bookingExchange()).with("wallet.refund");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
