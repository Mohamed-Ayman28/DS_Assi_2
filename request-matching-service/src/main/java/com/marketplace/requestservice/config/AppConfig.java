package com.marketplace.requestservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    public static final String NOTIFICATION_EXCHANGE        = "notification.exchange";
    public static final String REQUEST_EXCHANGE             = "request.exchange";
    public static final String PROVIDER_NEW_REQUEST_QUEUE   = "provider.new.request.queue";

    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean public TopicExchange notificationExchange() { return new TopicExchange(NOTIFICATION_EXCHANGE); }
    @Bean public TopicExchange requestExchange()      { return new TopicExchange(REQUEST_EXCHANGE); }

    @Bean public Queue providerNewRequestQueue() {
        return QueueBuilder.durable(PROVIDER_NEW_REQUEST_QUEUE).build();
    }
    @Bean public Binding providerNewRequestBinding() {
        return BindingBuilder.bind(providerNewRequestQueue()).to(requestExchange()).with("request.new.#");
    }

    @Bean public Jackson2JsonMessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate t = new RabbitTemplate(cf);
        t.setMessageConverter(messageConverter());
        return t;
    }
}
