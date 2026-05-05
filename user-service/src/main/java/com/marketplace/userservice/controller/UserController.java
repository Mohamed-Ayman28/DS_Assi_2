package com.marketplace.userservice.controller;

import com.marketplace.userservice.dto.UserDto;
import com.marketplace.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // ── Registration ──────────────────────────────────────────────────────────

    @PostMapping("/api/customers/register")
    public ResponseEntity<UserDto.AuthResponse> registerCustomer(
            @Valid @RequestBody UserDto.CustomerRegisterRequest request) {
        return ResponseEntity.ok(userService.registerCustomer(request));
    }

    @PostMapping("/api/providers/register")
    public ResponseEntity<UserDto.AuthResponse> registerProvider(
            @Valid @RequestBody UserDto.ProviderRegisterRequest request) {
        return ResponseEntity.ok(userService.registerProvider(request));
    }

    // ── Login (shared) ────────────────────────────────────────────────────────

    @PostMapping("/api/auth/login")
    public ResponseEntity<UserDto.AuthResponse> login(
            @Valid @RequestBody UserDto.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // ── Wallet ────────────────────────────────────────────────────────────────

    @PostMapping("/api/customers/{userId}/wallet/add")
    public ResponseEntity<UserDto.WalletResponse> addFunds(
            @PathVariable Long userId,
            @Valid @RequestBody UserDto.AddFundsRequest request) {
        return ResponseEntity.ok(userService.addFunds(userId, request.getAmount()));
    }

    @GetMapping("/api/customers/{userId}/wallet")
    public ResponseEntity<UserDto.WalletResponse> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getWallet(userId));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @GetMapping("/api/admin/users")
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/api/admin/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        return ResponseEntity.ok(userService.getSystemInfo());
    }

    @PostMapping("/api/admin/categories")
    public ResponseEntity<Map<String, Object>> addCategory(@RequestBody Map<String, String> body) {
        String category = body.get("category");
        boolean added = userService.addServiceCategory(category);
        return ResponseEntity.ok(Map.of(
                "success", added,
                "message", added ? "Category added: " + category : "Category already exists",
                "categories", userService.getAllCategories()
        ));
    }

    @GetMapping("/api/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(userService.getAllCategories());
    }

    // ── Internal (called by other services) ───────────────────────────────────

    @GetMapping("/api/internal/users/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/api/internal/users/by-username/{username}")
    public ResponseEntity<UserDto.UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
}
