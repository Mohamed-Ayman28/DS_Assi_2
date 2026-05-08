package com.marketplace.userservice.ejb;

import jakarta.ejb.Stateless;
import com.marketplace.userservice.dto.UserDto;
import com.marketplace.userservice.entity.User;
import com.marketplace.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Stateless
@Component
@RequiredArgsConstructor
@Slf4j
public class UserManagementBean {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User registerCustomer(UserDto.CustomerRegisterRequest request) {
        validateUniqueUsername(request.getUsername());
        validateUniqueEmail(request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(User.UserRole.CUSTOMER)
                .walletBalance(request.getInitialBalance())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("[EJB-Stateless] Customer registered: {}", saved.getUsername());
        return saved;
    }

    @Transactional
    public User registerProvider(UserDto.ProviderRegisterRequest request) {
        validateUniqueUsername(request.getUsername());
        validateUniqueEmail(request.getEmail());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(User.UserRole.SERVICE_PROVIDER)
                .professionType(request.getProfessionType())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("[EJB-Stateless] Service provider registered: {} ({})", saved.getUsername(), saved.getProfessionType());
        return saved;
    }


    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }



    @Transactional
    public User addFunds(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (user.getRole() != User.UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customers have wallets");
        }
        user.setWalletBalance(user.getWalletBalance().add(amount));
        log.info("[EJB-Stateless] Wallet topped up for {}: +{}", user.getUsername(), amount);
        return userRepository.save(user);
    }

    @Transactional
    public boolean deductFunds(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (user.getWalletBalance().compareTo(amount) < 0) {
            log.warn("[EJB-Stateless] Insufficient balance for userId={}: has {}, needs {}", userId, user.getWalletBalance(), amount);
            return false;
        }
        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        userRepository.save(user);
        log.info("[EJB-Stateless] Deducted {} from userId={}", amount, userId);
        return true;
    }

    @Transactional
    public void refundFunds(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        userRepository.save(user);
        log.info("[EJB-Stateless] Refunded {} to userId={}", amount, userId);
    }


    private void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
    }

    private void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
    }
}
