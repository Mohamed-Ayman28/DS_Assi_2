package com.marketplace.offerservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserById(Long userId) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    userServiceUrl + "/api/internal/users/" + userId,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch user {}: {}", userId, e.getMessage());
            throw new IllegalArgumentException("Could not verify user: " + userId);
        }
    }
}
