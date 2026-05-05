package com.marketplace.bookingservice.dto;

import com.marketplace.bookingservice.entity.Booking;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateBookingRequest {
        @NotNull private Long customerId;
        @NotNull private Long offerId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BookingResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private Long offerId;
        private Long providerId;
        private String providerName;
        private String professionType;
        private String category;
        private BigDecimal amount;
        private LocalDate serviceDate;
        private String status;
        private String rejectionReason;
        private LocalDateTime createdAt;

        public static BookingResponse fromEntity(Booking b) {
            return BookingResponse.builder()
                    .id(b.getId())
                    .customerId(b.getCustomerId())
                    .customerName(b.getCustomerName())
                    .offerId(b.getOfferId())
                    .providerId(b.getProviderId())
                    .providerName(b.getProviderName())
                    .professionType(b.getProfessionType())
                    .category(b.getCategory())
                    .amount(b.getAmount())
                    .serviceDate(b.getServiceDate())
                    .status(b.getStatus().name())
                    .rejectionReason(b.getRejectionReason())
                    .createdAt(b.getCreatedAt())
                    .build();
        }
    }
}
