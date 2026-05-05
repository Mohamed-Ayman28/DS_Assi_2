package com.marketplace.offerservice.repository;

import com.marketplace.offerservice.entity.ServiceOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    List<ServiceOffer> findByProviderIdAndStatus(Long providerId, ServiceOffer.OfferStatus status);
    List<ServiceOffer> findByStatus(ServiceOffer.OfferStatus status);
    List<ServiceOffer> findByCategoryIgnoreCaseAndStatus(String category, ServiceOffer.OfferStatus status);
    List<ServiceOffer> findByProviderId(Long providerId);

    @Query("SELECT o FROM ServiceOffer o WHERE o.status = 'ACTIVE' AND " +
           "LOWER(o.category) = LOWER(:category) AND " +
           "o.availableDate = :date AND " +
           "o.price <= :maxPrice")
    List<ServiceOffer> findMatchingOffers(String category, LocalDate date, BigDecimal maxPrice);

    Optional<ServiceOffer> findByIdAndProviderId(Long id, Long providerId);
}
