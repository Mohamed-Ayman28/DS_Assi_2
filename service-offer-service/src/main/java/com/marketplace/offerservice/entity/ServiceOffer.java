package com.marketplace.offerservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_offers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String professionType;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    private Long bookedByCustomerId;
    private String bookedByCustomerName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OfferStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OfferStatus {
        ACTIVE, BOOKED, CANCELLED, COMPLETED
    }
}
