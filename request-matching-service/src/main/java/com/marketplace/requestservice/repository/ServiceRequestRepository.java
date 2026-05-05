package com.marketplace.requestservice.repository;

import com.marketplace.requestservice.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByCustomerId(Long customerId);
    List<ServiceRequest> findByStatus(ServiceRequest.RequestStatus status);
    List<ServiceRequest> findByMatchedProviderIdAndStatus(Long providerId, ServiceRequest.RequestStatus status);
}
