package com.marketplace.bookingservice.client;

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
public class OfferServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.offer-service-url}")
    private String offerServiceUrl;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getOfferById(Long offerId) {
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    offerServiceUrl + "/api/internal/offers/" + offerId,
                    HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch offer {}: {}", offerId, e.getMessage());
            throw new IllegalArgumentException("Offer not found: " + offerId);
        }
    }

    public void bookOffer(Long offerId, Map<String, Object> body) {
        try {
            restTemplate.postForEntity(
                    offerServiceUrl + "/api/internal/offers/" + offerId + "/book",
                    body, Map.class);
        } catch (Exception e) {
            log.error("Failed to reserve offer {}: {}", offerId, e.getMessage());
            throw new IllegalStateException("Failed to reserve offer: " + e.getMessage());
        }
    }

    public void completeOffer(Long offerId) {
        try {
            restTemplate.postForEntity(
                    offerServiceUrl + "/api/internal/offers/" + offerId + "/complete",
                    null, Void.class);
        } catch (Exception e) {
            log.error("Failed to complete offer {}: {}", offerId, e.getMessage());
        }
    }

    public void releaseOffer(Long offerId) {
        try {
            restTemplate.postForEntity(
                    offerServiceUrl + "/api/internal/offers/" + offerId + "/release",
                    null, Void.class);
        } catch (Exception e) {
            log.error("Failed to release offer {}: {}", offerId, e.getMessage());
        }
    }
}
