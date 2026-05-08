package com.marketplace.offerservice.controller;

import com.marketplace.offerservice.dto.OfferDto;
import com.marketplace.offerservice.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class OfferController {

    @Autowired
    private OfferService offerService;

    @PostMapping("/api/offers")
    public ResponseEntity<Map<String, String>> createOffer(
            @Valid @RequestBody OfferDto.CreateOfferRequest request) {
        return ResponseEntity.ok(offerService.createOffer(request));
    }

    @PutMapping("/api/offers/{offerId}/provider/{providerId}")
    public ResponseEntity<Map<String, String>> updateOffer(
            @PathVariable Long offerId,
            @PathVariable Long providerId,
            @RequestBody OfferDto.UpdateOfferRequest request) {
        return ResponseEntity.ok(offerService.updateOffer(offerId, providerId, request));
    }

    @GetMapping("/api/offers/provider/{providerId}")
    public ResponseEntity<List<OfferDto.OfferResponse>> getProviderOffers(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(offerService.getProviderOffers(providerId));
    }

    @GetMapping("/api/offers/provider/{providerId}/completed")
    public ResponseEntity<List<OfferDto.OfferResponse>> getProviderCompletedOffers(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(offerService.getProviderCompletedOffers(providerId));
    }

    @GetMapping("/api/offers")
    public ResponseEntity<List<OfferDto.OfferResponse>> getActiveOffers() {
        return ResponseEntity.ok(offerService.getActiveOffers());
    }

    @GetMapping("/api/offers/category/{category}")
    public ResponseEntity<List<OfferDto.OfferResponse>> getOffersByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(offerService.getOffersByCategory(category));
    }

    @GetMapping("/api/offers/search")
    public ResponseEntity<List<OfferDto.OfferResponse>> searchOffers(
            @RequestParam String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(offerService.findMatchingOffers(category, date, maxPrice));
    }

    @PostMapping("/api/internal/offers/{offerId}/book")
    public ResponseEntity<Map<String, String>> bookOffer(
            @PathVariable Long offerId,
            @RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        String customerName = (String) body.get("customerName");
        return ResponseEntity.ok(offerService.bookOffer(offerId, customerId, customerName));
    }

    @PostMapping("/api/internal/offers/{offerId}/complete")
    public ResponseEntity<Void> completeOffer(@PathVariable Long offerId) {
        offerService.completeOffer(offerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/internal/offers/{offerId}/release")
    public ResponseEntity<Void> releaseOffer(@PathVariable Long offerId) {
        offerService.releaseOffer(offerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/internal/offers/{offerId}")
    public ResponseEntity<OfferDto.OfferResponse> getOfferById(@PathVariable Long offerId) {
        return ResponseEntity.ok(offerService.getOfferById(offerId));
    }
}
