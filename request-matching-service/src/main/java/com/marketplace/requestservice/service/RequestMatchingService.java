package com.marketplace.requestservice.service;

import com.marketplace.requestservice.client.ServiceClients;
import com.marketplace.requestservice.config.AppConfig;
import com.marketplace.requestservice.dto.RequestDto;
import com.marketplace.requestservice.entity.ServiceRequest;
import com.marketplace.requestservice.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestMatchingService {

    private final ServiceRequestRepository requestRepository;
    private final ServiceClients serviceClients;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @SuppressWarnings("null")
    public RequestDto.ServiceRequestResponse createServiceRequest(RequestDto.CreateServiceRequestRequest req, String authorization) {
        Map<String, Object> customer = serviceClients.getUserById(req.getCustomerId(), authorization);
        if (!"CUSTOMER".equals(customer.get("role"))) {
            throw new IllegalArgumentException("Only customers can create service requests");
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .customerId(req.getCustomerId())
                .customerName((String) customer.get("username"))
                .category(req.getCategory())
                .description(req.getDescription())
                .maxPrice(req.getMaxPrice())
                .requiredDate(req.getRequiredDate())
                .status(ServiceRequest.RequestStatus.OPEN)
                .build();

        serviceRequest = requestRepository.save(serviceRequest);
        log.info("Service request #{} created by customerId={}", serviceRequest.getId(), req.getCustomerId());

        List<Map<String, Object>> matchingOffers = serviceClients.searchMatchingOffers(
                req.getCategory(), req.getRequiredDate(), req.getMaxPrice());

        if (matchingOffers.isEmpty()) {
            log.info("No matching offers found for request #{}", serviceRequest.getId());
        } else {
            log.info("Found {} matching offers for request #{}", matchingOffers.size(), serviceRequest.getId());

            for (Map<String, Object> offer : matchingOffers) {
                Long providerId = Long.valueOf(offer.get("providerId").toString());

                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "NEW_SERVICE_REQUEST");
                notification.put("userId", providerId);
                notification.put("username", offer.get("providerName"));
                notification.put("requestId", serviceRequest.getId());
                notification.put("customerId", serviceRequest.getCustomerId());
                notification.put("customerName", serviceRequest.getCustomerName());
                notification.put("category", serviceRequest.getCategory());
                notification.put("description", serviceRequest.getDescription());
                notification.put("maxPrice", serviceRequest.getMaxPrice());
                notification.put("requiredDate", serviceRequest.getRequiredDate().toString());
                notification.put("matchedOfferId", offer.get("id"));
                notification.put("message", "New service request from " + serviceRequest.getCustomerName()
                        + " for " + serviceRequest.getCategory()
                        + " on " + serviceRequest.getRequiredDate()
                        + " (budget: $" + serviceRequest.getMaxPrice() + "). Request #" + serviceRequest.getId());
                notification.put("timestamp", LocalDateTime.now().toString());

                rabbitTemplate.convertAndSend(
                        AppConfig.REQUEST_EXCHANGE,
                        "request.new." + providerId,
                        notification
                );
                rabbitTemplate.convertAndSend(
                        AppConfig.NOTIFICATION_EXCHANGE,
                        "notification.provider." + providerId,
                        notification
                );
                log.info("Notified provider {} of new service request #{}", providerId, serviceRequest.getId());
            }

            Map<String, Object> firstOffer = matchingOffers.get(0);
            serviceRequest.setMatchedOfferId(Long.valueOf(firstOffer.get("id").toString()));
            serviceRequest.setMatchedProviderId(Long.valueOf(firstOffer.get("providerId").toString()));
            serviceRequest.setMatchedProviderName((String) firstOffer.get("providerName"));
            serviceRequest.setStatus(ServiceRequest.RequestStatus.MATCHED);
            serviceRequest = requestRepository.save(serviceRequest);
        }

        return RequestDto.ServiceRequestResponse.fromEntity(serviceRequest);
    }


    @Transactional
    @SuppressWarnings("null")
    public RequestDto.ServiceRequestResponse providerRespondToRequest(
            Long requestId, RequestDto.ProviderResponseRequest providerResponse, String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException("Authorization header is required to accept or reject a service request");
        }

        ServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found: " + requestId));

        if (request.getStatus() != ServiceRequest.RequestStatus.MATCHED &&
                request.getStatus() != ServiceRequest.RequestStatus.OPEN) {
            throw new IllegalStateException("Request is not in a state that can be responded to. Status: " + request.getStatus());
        }

        if (!providerResponse.getProviderId().equals(request.getMatchedProviderId())) {
            throw new IllegalArgumentException("Only the matched provider can respond to this request");
        }

        if ("ACCEPTED".equalsIgnoreCase(providerResponse.getResponse())) {
            request.setStatus(ServiceRequest.RequestStatus.ACCEPTED);
            request.setProviderResponse("ACCEPTED");
            request = requestRepository.save(request);

            try {
                Map<String, Object> booking = serviceClients.createBooking(
                        request.getCustomerId(), request.getMatchedOfferId(), authorization);

                String bookingStatus = (String) booking.get("status");
                Long bookingId = Long.valueOf(booking.get("id").toString());
                request.setBookingId(bookingId);

                if ("CONFIRMED".equals(bookingStatus)) {
                    request.setStatus(ServiceRequest.RequestStatus.BOOKED);
                    log.info("Request #{} booked successfully. BookingId={}", requestId, bookingId);
                } else {
                    request.setStatus(ServiceRequest.RequestStatus.REJECTED);
                    request.setProviderResponse("BOOKING_FAILED: " + booking.get("rejectionReason"));
                    log.warn("Booking failed for request #{}", requestId);
                }

            } catch (Exception e) {
                request.setStatus(ServiceRequest.RequestStatus.REJECTED);
                request.setProviderResponse("BOOKING_FAILED: " + e.getMessage());
                log.error("Failed to create booking for request #{}: {}", requestId, e.getMessage());
            }

            String outcomeMsg = ServiceRequest.RequestStatus.BOOKED == request.getStatus()
                    ? "Your service request #" + requestId + " has been accepted by "
                        + request.getMatchedProviderName() + " and a booking is confirmed!"
                    : "Your service request #" + requestId + " was accepted but booking failed. Please try again.";

            sendCustomerNotification(request.getCustomerId(), request.getCustomerName(),
                    request.getStatus() == ServiceRequest.RequestStatus.BOOKED
                            ? "REQUEST_ACCEPTED_AND_BOOKED" : "REQUEST_BOOKING_FAILED",
                    outcomeMsg, requestId);

        } else {
            request.setStatus(ServiceRequest.RequestStatus.REJECTED);
            request.setProviderResponse("REJECTED");
            request = requestRepository.save(request);

            sendCustomerNotification(request.getCustomerId(), request.getCustomerName(),
                    "REQUEST_REJECTED",
                    "Your service request #" + requestId + " was declined by the provider. Consider browsing other offers.",
                    requestId);
            log.info("Request #{} rejected by provider {}", requestId, providerResponse.getProviderId());
        }

        return RequestDto.ServiceRequestResponse.fromEntity(requestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<RequestDto.ServiceRequestResponse> getCustomerRequests(Long customerId) {
        return requestRepository.findByCustomerId(customerId).stream()
                .map(RequestDto.ServiceRequestResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestDto.ServiceRequestResponse> getOpenRequests() {
        return requestRepository.findByStatus(ServiceRequest.RequestStatus.OPEN).stream()
                .map(RequestDto.ServiceRequestResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public RequestDto.ServiceRequestResponse getRequestById(Long id) {
        return requestRepository.findById(id)
                .map(RequestDto.ServiceRequestResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
    }

    private void sendCustomerNotification(Long customerId, String customerName,
                                           String type, String message, Long requestId) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("userId", customerId);
            notification.put("username", customerName);
            notification.put("message", message);
            notification.put("bookingId", requestId);
            notification.put("timestamp", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(
                    AppConfig.NOTIFICATION_EXCHANGE,
                    "notification.customer." + customerId,
                    notification
            );
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
