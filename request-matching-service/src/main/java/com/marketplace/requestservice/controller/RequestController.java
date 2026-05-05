package com.marketplace.requestservice.controller;

import com.marketplace.requestservice.dto.RequestDto;
import com.marketplace.requestservice.service.RequestMatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {

    private final RequestMatchingService requestMatchingService;

    /** Customer: create a new service request */
    @PostMapping("/api/service-requests")
    public ResponseEntity<RequestDto.ServiceRequestResponse> createRequest(
            @Valid @RequestBody RequestDto.CreateServiceRequestRequest request) {
        return ResponseEntity.ok(requestMatchingService.createServiceRequest(request));
    }

    /** Provider: accept or reject a matched request */
    @PostMapping("/api/service-requests/{requestId}/respond")
    public ResponseEntity<RequestDto.ServiceRequestResponse> providerRespond(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestDto.ProviderResponseRequest response) {
        return ResponseEntity.ok(requestMatchingService.providerRespondToRequest(requestId, response));
    }

    /** Customer: view their own service requests */
    @GetMapping("/api/service-requests/customer/{customerId}")
    public ResponseEntity<List<RequestDto.ServiceRequestResponse>> getCustomerRequests(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(requestMatchingService.getCustomerRequests(customerId));
    }

    /** Public: view all open service requests */
    @GetMapping("/api/service-requests/open")
    public ResponseEntity<List<RequestDto.ServiceRequestResponse>> getOpenRequests() {
        return ResponseEntity.ok(requestMatchingService.getOpenRequests());
    }

    /** Get a single request by ID */
    @GetMapping("/api/service-requests/{id}")
    public ResponseEntity<RequestDto.ServiceRequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestMatchingService.getRequestById(id));
    }
}
