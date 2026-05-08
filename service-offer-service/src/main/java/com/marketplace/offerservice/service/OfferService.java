package com.marketplace.offerservice.service;

import com.marketplace.offerservice.client.UserServiceClient;
import com.marketplace.offerservice.dto.OfferDto;
import com.marketplace.offerservice.entity.ServiceOffer;
import com.marketplace.offerservice.repository.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final ServiceOfferRepository offerRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Map<String, String> createOffer(OfferDto.CreateOfferRequest request, String authorization) {

        Map<String, Object> provider = userServiceClient.getUserById(request.getProviderId(), authorization);
        String role = (String) provider.get("role");
        if (!"SERVICE_PROVIDER".equals(role)) {
            throw new IllegalArgumentException("Only service providers can create offers");
        }

        String providerName = (String) provider.get("username");

        ServiceOffer offer = ServiceOffer.builder()
                .providerId(request.getProviderId())
                .providerName(providerName)
                .professionType((String) provider.get("professionType"))
                .category(request.getCategory())
                .description(request.getDescription())
                .price(request.getPrice())
                .availableDate(request.getAvailableDate())
                .status(ServiceOffer.OfferStatus.ACTIVE)
                .build();

        offerRepository.save(offer);

        return Map.of("message", "Successfully created offer by provider name: " + providerName);
    }
    @Transactional
    public Map<String, String> updateOffer(Long offerId, Long providerId, OfferDto.UpdateOfferRequest request) {
        ServiceOffer offer = offerRepository.findByIdAndProviderId(offerId, providerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found or not owned by provider"));

        if (offer.getStatus() != ServiceOffer.OfferStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE offers can be updated");
        }

        if (request.getPrice() != null) offer.setPrice(request.getPrice());
        if (request.getAvailableDate() != null) offer.setAvailableDate(request.getAvailableDate());
        if (request.getDescription() != null) offer.setDescription(request.getDescription());

        offerRepository.save(offer);

        return Map.of("message", "Offer " + offerId + " is updated successfully by provider name: " + offer.getProviderName());
    }

    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> getActiveOffers() {
        return offerRepository.findByStatus(ServiceOffer.OfferStatus.ACTIVE).stream()
                .map(OfferDto.OfferResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> getOffersByCategory(String category) {
        return offerRepository.findByCategoryIgnoreCaseAndStatus(category, ServiceOffer.OfferStatus.ACTIVE)
                .stream().map(OfferDto.OfferResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> getProviderOffers(Long providerId) {
        return offerRepository.findByProviderId(providerId).stream()
                .map(OfferDto.OfferResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> getProviderCompletedOffers(Long providerId) {
        return offerRepository.findByProviderIdAndStatus(providerId, ServiceOffer.OfferStatus.COMPLETED)
                .stream().map(OfferDto.OfferResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OfferDto.OfferResponse> findMatchingOffers(String category, LocalDate date, BigDecimal maxPrice) {
        return offerRepository.findMatchingOffers(category, date, maxPrice).stream()
                .map(OfferDto.OfferResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, String> bookOffer(Long offerId, Long customerId, String customerName) {
        ServiceOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        if (offer.getStatus() != ServiceOffer.OfferStatus.ACTIVE) {
            throw new IllegalStateException("Offer is not available (status=" + offer.getStatus() + ")");
        }
        offer.setStatus(ServiceOffer.OfferStatus.BOOKED);
        offer.setBookedByCustomerId(customerId);
        offer.setBookedByCustomerName(customerName);
        offerRepository.save(offer);

        return Map.of("message", "Offer " + offerId + " is successfully booked by " + customerName);
    }

    @Transactional
    public void completeOffer(Long offerId) {
        offerRepository.findById(offerId).ifPresent(offer -> {
            offer.setStatus(ServiceOffer.OfferStatus.COMPLETED);
            offerRepository.save(offer);
        });
    }

    @Transactional
    public void releaseOffer(Long offerId) {
        offerRepository.findById(offerId).ifPresent(offer -> {
            offer.setStatus(ServiceOffer.OfferStatus.ACTIVE);
            offer.setBookedByCustomerId(null);
            offer.setBookedByCustomerName(null);
            offerRepository.save(offer);
            log.info("Offer {} released back to ACTIVE", offerId);
        });
    }

    @Transactional(readOnly = true)
    public OfferDto.OfferResponse getOfferById(Long id) {
        return offerRepository.findById(id)
                .map(OfferDto.OfferResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));
    }
}
