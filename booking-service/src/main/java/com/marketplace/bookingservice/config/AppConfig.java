package com.marketplace.bookingservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    // Exchanges
    public static final String BOOKING_EXCHANGE = "booking.exchange";
    public static final String PAYMENTS_EXCHANGE = "payments.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Queues
    public static final String WALLET_DEDUCT_QUEUE = "wallet.deduct.queue";
    public static final String WALLET_REFUND_QUEUE = "wallet.refund.queue";
    public static final String BOOKING_CONFIRM_QUEUE = "booking.confirm.queue";
    public static final String BOOKING_REJECT_QUEUE = "booking.reject.queue";
    public static final String ADMIN_PAYMENT_FAILED_QUEUE = "admin.payment.failed.queue";
    public static final String NOTIFICATION_CUSTOMER_QUEUE = "notification.customer.queue";
    public static final String NOTIFICATION_PROVIDER_QUEUE = "notification.provider.queue";

    public static final String PAYMENT_FAILED_KEY = "PaymentFailed";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

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
    public Queue bookingConfirmQueue() {
        return QueueBuilder.durable(BOOKING_CONFIRM_QUEUE).build();
    }

    @Bean
    public Queue bookingRejectQueue() {
        return QueueBuilder.durable(BOOKING_REJECT_QUEUE).build();
    }

    @Bean
    public Queue adminPaymentFailedQueue() {
        return QueueBuilder.durable(ADMIN_PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Queue notificationCustomerQueue() {
        return QueueBuilder.durable(NOTIFICATION_CUSTOMER_QUEUE).build();
    }

    @Bean
    public Queue notificationProviderQueue() {
        return QueueBuilder.durable(NOTIFICATION_PROVIDER_QUEUE).build();
    }

    // Bindings for booking exchange
    @Bean
    public Binding walletDeductBinding() {
        return BindingBuilder.bind(walletDeductQueue()).to(bookingExchange()).with("wallet.deduct");
    }

    @Bean
    public Binding walletRefundBinding() {
        return BindingBuilder.bind(walletRefundQueue()).to(bookingExchange()).with("wallet.refund");
    }

    @Bean
    public Binding bookingConfirmBinding() {
        return BindingBuilder.bind(bookingConfirmQueue()).to(bookingExchange()).with("booking.confirm");
    }

    @Bean
    public Binding bookingRejectBinding() {
        return BindingBuilder.bind(bookingRejectQueue()).to(bookingExchange()).with("booking.reject");
    }

    // Direct exchange binding for admin PaymentFailed notifications
    @Bean
    public Binding adminPaymentFailedBinding() {
        return BindingBuilder.bind(adminPaymentFailedQueue()).to(paymentsExchange()).with(PAYMENT_FAILED_KEY);
    }

    // Notification bindings
    @Bean
    public Binding notificationCustomerBinding() {
        return BindingBuilder.bind(notificationCustomerQueue()).to(notificationExchange()).with("notification.customer.#");
    }

    @Bean
    public Binding notificationProviderBinding() {
        return BindingBuilder.bind(notificationProviderQueue()).to(notificationExchange()).with("notification.provider.#");
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
