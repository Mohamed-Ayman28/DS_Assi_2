package com.marketplace.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Service — Microservice #1
 *
 * Responsibilities:
 *  - Customer and Service Provider registration & authentication
 *  - Wallet management (balance, deduction, refund)
 *  - Service category management (Admin)
 *  - System registry (EJB Singleton)
 *
 * EJB Types Used:
 *  1. Stateless Session Bean  → UserManagementBean  (user CRUD + wallet ops)
 *  2. Singleton Session Bean  → SystemRegistryBean  (categories + system stats)
 *
 * RabbitMQ:
 *  - Listens on wallet.deduct.queue and wallet.refund.queue
 *  - Simulates Message-Driven Bean for async wallet processing
 */
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
