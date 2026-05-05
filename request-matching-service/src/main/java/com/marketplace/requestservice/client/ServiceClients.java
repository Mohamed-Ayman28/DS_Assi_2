package com.marketplace.requestservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceClients {

    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    @Value("${services.offer-service-url}")
    private String offerServiceUrl;

    @Value("${services.booking-service-url}")
    private String bookingServiceUrl;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserById(Long userId) {
        try {
            ResponseEntity<Map<String, Object>> r = restTemplate.exchange(
                    userServiceUrl + "/api/internal/users/" + userId,
                    HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return r.getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchMatchingOffers(String category, LocalDate date, BigDecimal maxPrice) {
        try {
            String url = offerServiceUrl + "/api/offers/search?category=" + category
                    + "&date=" + date + "&maxPrice=" + maxPrice;
            ResponseEntity<List<Map<String, Object>>> r = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return r.getBody();
        } catch (Exception e) {
            log.warn("No matching offers found: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> createBooking(Long customerId, Long offerId) {
        try {
            Map<String, Object> body = Map.of("customerId", customerId, "offerId", offerId);
            ResponseEntity<Map<String, Object>> r = restTemplate.exchange(
                    bookingServiceUrl + "/api/bookings",
                    HttpMethod.POST,
                    new HttpEntity<>(body, jsonHeaders()),
                    new ParameterizedTypeReference<>() {});
            return r.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Booking failed: " + e.getMessage());
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
