package com.marketplace.offerservice.dto;

import com.marketplace.offerservice.entity.ServiceOffer;
import lombok.*;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OfferDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateOfferRequest {
        @NotNull private Long providerId;
        @NotBlank private String category;
        @NotBlank private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @NotNull @FutureOrPresent private LocalDate availableDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateOfferRequest {
        @DecimalMin("0.01") private BigDecimal price;
        @Future private LocalDate availableDate;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OfferResponse {
        private Long id;
        private Long providerId;
        private String providerName;
        private String professionType;
        private String category;
        private String description;
        private BigDecimal price;
        private LocalDate availableDate;
        private String status;
        private Long bookedByCustomerId;
        private String bookedByCustomerName;
        private LocalDateTime createdAt;

        public static OfferResponse fromEntity(ServiceOffer o) {
            return OfferResponse.builder()
                    .id(o.getId())
                    .providerId(o.getProviderId())
                    .providerName(o.getProviderName())
                    .professionType(o.getProfessionType())
                    .category(o.getCategory())
                    .description(o.getDescription())
                    .price(o.getPrice())
                    .availableDate(o.getAvailableDate())
                    .status(o.getStatus().name())
                    .bookedByCustomerId(o.getBookedByCustomerId())
                    .bookedByCustomerName(o.getBookedByCustomerName())
                    .createdAt(o.getCreatedAt())
                    .build();
        }
    }

    // Internal DTO for booking service
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BookOfferRequest {
        private Long offerId;
        private Long customerId;
        private String customerName;
    }
}
