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

    @PostMapping("/api/service-requests")
    public ResponseEntity<RequestDto.ServiceRequestResponse> createRequest(
            @Valid @RequestBody RequestDto.CreateServiceRequestRequest request) {
        return ResponseEntity.ok(requestMatchingService.createServiceRequest(request));
    }

    @PostMapping("/api/service-requests/{requestId}/respond")
    public ResponseEntity<RequestDto.ServiceRequestResponse> providerRespond(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestDto.ProviderResponseRequest response) {
        return ResponseEntity.ok(requestMatchingService.providerRespondToRequest(requestId, response));
    }

    @GetMapping("/api/service-requests/customer/{customerId}")
    public ResponseEntity<List<RequestDto.ServiceRequestResponse>> getCustomerRequests(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(requestMatchingService.getCustomerRequests(customerId));
    }

    @GetMapping("/api/service-requests/open")
    public ResponseEntity<List<RequestDto.ServiceRequestResponse>> getOpenRequests() {
        return ResponseEntity.ok(requestMatchingService.getOpenRequests());
    }

    @GetMapping("/api/service-requests/{id}")
    public ResponseEntity<RequestDto.ServiceRequestResponse> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestMatchingService.getRequestById(id));
    }
}
