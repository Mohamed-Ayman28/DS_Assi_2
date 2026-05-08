package com.marketplace.requestservice.dto;

import com.marketplace.requestservice.entity.ServiceRequest;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateServiceRequestRequest {
        @NotNull  private Long customerId;
        @NotBlank private String category;
        @NotBlank private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal maxPrice;
        @NotNull  private LocalDate requiredDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceRequestResponse {
        private Long id;
        private Long customerId;
        private String customerName;
        private String category;
        private String description;
        private BigDecimal maxPrice;
        private LocalDate requiredDate;
        private String status;
        private Long matchedOfferId;
        private Long matchedProviderId;
        private String matchedProviderName;
        private Long bookingId;
        private LocalDateTime createdAt;

        public static ServiceRequestResponse fromEntity(ServiceRequest r) {
            return ServiceRequestResponse.builder()
                    .id(r.getId())
                    .customerId(r.getCustomerId())
                    .customerName(r.getCustomerName())
                    .category(r.getCategory())
                    .description(r.getDescription())
                    .maxPrice(r.getMaxPrice())
                    .requiredDate(r.getRequiredDate())
                    .status(r.getStatus().name())
                    .matchedOfferId(r.getMatchedOfferId())
                    .matchedProviderId(r.getMatchedProviderId())
                    .matchedProviderName(r.getMatchedProviderName())
                    .bookingId(r.getBookingId())
                    .createdAt(r.getCreatedAt())
                    .build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProviderResponseRequest {
        @NotNull  private Long providerId;
        @NotBlank private String response;
    }
}
