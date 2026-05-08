package com.marketplace.userservice.service;

import com.marketplace.userservice.config.JwtUtil;
import com.marketplace.userservice.dto.UserDto;
import com.marketplace.userservice.ejb.SystemRegistryBean;
import com.marketplace.userservice.ejb.UserManagementBean;
import com.marketplace.userservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserManagementBean userManagementBean;
    private final SystemRegistryBean systemRegistryBean;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserDto.AuthResponse registerCustomer(UserDto.CustomerRegisterRequest request) {
        User user = userManagementBean.registerCustomer(request);
        systemRegistryBean.incrementAndGetRegistrations();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return UserDto.AuthResponse.builder()
                .token(token).username(user.getUsername())
                .role(user.getRole().name()).userId(user.getId()).build();
    }

    public UserDto.AuthResponse registerProvider(UserDto.ProviderRegisterRequest request) {
        User user = userManagementBean.registerProvider(request);
        systemRegistryBean.incrementAndGetRegistrations();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return UserDto.AuthResponse.builder()
                .token(token).username(user.getUsername())
                .role(user.getRole().name()).userId(user.getId()).build();
    }

    public UserDto.AuthResponse login(UserDto.LoginRequest request) {
        User user = userManagementBean.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());

        return UserDto.AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .userId(user.getId())
                .message("Login successful")
                .build();
    }

    public UserDto.WalletResponse addFunds(Long userId, BigDecimal amount) {
        User user = userManagementBean.addFunds(userId, amount);
        return UserDto.WalletResponse.builder()
                .userId(user.getId()).username(user.getUsername())
                .balance(user.getWalletBalance()).build();
    }

    public UserDto.WalletResponse getWallet(Long userId) {
        User user = userManagementBean.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserDto.WalletResponse.builder()
                .userId(user.getId()).username(user.getUsername())
                .balance(user.getWalletBalance()).build();
    }

    public List<UserDto.UserResponse> getAllUsers() {
        return userManagementBean.findAllUsers().stream()
                .map(UserDto.UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto.UserResponse getUserById(Long id) {
        return userManagementBean.findById(id)
                .map(UserDto.UserResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public UserDto.UserResponse getUserByUsername(String username) {
        return userManagementBean.findByUsername(username)
                .map(UserDto.UserResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public boolean addServiceCategory(String category) {
        return systemRegistryBean.addCategory(category);
    }

    public List<String> getAllCategories() {
        return systemRegistryBean.getAllCategories();
    }

    public java.util.Map<String, Object> getSystemInfo() {
        return systemRegistryBean.getSystemInfo();
    }
}
