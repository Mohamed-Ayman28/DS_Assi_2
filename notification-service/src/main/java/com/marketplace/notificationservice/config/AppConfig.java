package com.marketplace.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    public static final String NOTIFICATION_EXCHANGE    = "notification.exchange";
    public static final String PAYMENTS_EXCHANGE        = "payments.exchange";
    public static final String NOTIFICATION_CUSTOMER_Q  = "notification.customer.queue";
    public static final String NOTIFICATION_PROVIDER_Q  = "notification.provider.queue";
    public static final String ADMIN_PAYMENT_FAILED_Q   = "admin.payment.failed.queue";

    @Bean public TopicExchange notificationExchange()  { return new TopicExchange(NOTIFICATION_EXCHANGE); }
    @Bean public DirectExchange paymentsExchange()     { return new DirectExchange(PAYMENTS_EXCHANGE); }
    @Bean public Queue notificationCustomerQueue()     { return QueueBuilder.durable(NOTIFICATION_CUSTOMER_Q).build(); }
    @Bean public Queue notificationProviderQueue()     { return QueueBuilder.durable(NOTIFICATION_PROVIDER_Q).build(); }
    @Bean public Queue adminPaymentFailedQueue()       { return QueueBuilder.durable(ADMIN_PAYMENT_FAILED_Q).build(); }

    @Bean public Binding customerBinding() {
        return BindingBuilder.bind(notificationCustomerQueue()).to(notificationExchange()).with("notification.customer.#");
    }
    @Bean public Binding providerBinding() {
        return BindingBuilder.bind(notificationProviderQueue()).to(notificationExchange()).with("notification.provider.#");
    }
    @Bean public Binding adminPaymentBinding() {
        return BindingBuilder.bind(adminPaymentFailedQueue()).to(paymentsExchange()).with("PaymentFailed");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
