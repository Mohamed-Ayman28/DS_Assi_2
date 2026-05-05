package com.marketplace.requestservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false)
    private LocalDate requiredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    // Set when matched to an offer
    private Long matchedOfferId;
    private Long matchedProviderId;
    private String matchedProviderName;

    // Set when a booking is created from this request
    private Long bookingId;

    // Provider's response
    private String providerResponse; // ACCEPTED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = RequestStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum RequestStatus {
        OPEN,           // Customer placed request, looking for match
        MATCHED,        // Matched with an offer, waiting for provider acceptance
        ACCEPTED,       // Provider accepted → booking in progress
        BOOKED,         // Booking confirmed
        REJECTED,       // Provider rejected or no match found
        CANCELLED       // Customer cancelled
    }
}
