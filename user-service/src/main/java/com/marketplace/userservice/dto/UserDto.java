package com.marketplace.userservice.dto;

import com.marketplace.userservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CustomerRegisterRequest {
        @NotBlank private String username;
        @NotBlank @Size(min = 6) private String password;
        @NotBlank @Email private String email;
        @NotNull @DecimalMin("0.0") private BigDecimal initialBalance;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProviderRegisterRequest {
        @NotBlank private String username;
        @NotBlank @Size(min = 6) private String password;
        @NotBlank @Email private String email;
        @NotBlank private String professionType;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AddFundsRequest {
        @NotNull @DecimalMin("0.01") private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;
        private Long userId;
        private String message;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String professionType;
        private BigDecimal walletBalance;
        private boolean active;
        private LocalDateTime createdAt;

        public static UserResponse fromEntity(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .professionType(user.getProfessionType())
                    .walletBalance(user.getWalletBalance())
                    .active(user.isActive())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WalletResponse {
        private Long userId;
        private String username;
        private BigDecimal balance;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WalletDeductRequest {
        private Long userId;
        private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WalletRefundRequest {
        private Long userId;
        private BigDecimal amount;
        private String reason;
    }
}
