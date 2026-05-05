package com.marketplace.requestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Request Matching Service — Microservice #5 (Team of 3 Additional Feature)
 *
 * Responsibilities:
 *  - Accept customer service requests with category, price, and date
 *  - Match requests to available service offers
 *  - Notify matching providers via RabbitMQ
 *  - Handle provider acceptance/rejection and trigger bookings
 */
@SpringBootApplication
public class RequestMatchingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestMatchingServiceApplication.class, args);
    }
}
