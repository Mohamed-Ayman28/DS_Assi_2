package com.marketplace.bookingservice.controller;

import com.marketplace.bookingservice.dto.BookingDto;
import com.marketplace.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/api/bookings")
    public ResponseEntity<BookingDto.BookingResponse> createBooking(
            @Valid @RequestBody BookingDto.CreateBookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/api/bookings/customer/{customerId}")
    public ResponseEntity<List<BookingDto.BookingResponse>> getCustomerBookings(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(bookingService.getCustomerBookings(customerId));
    }

    @GetMapping("/api/bookings/provider/{providerId}")
    public ResponseEntity<List<BookingDto.BookingResponse>> getProviderBookings(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(bookingService.getProviderBookings(providerId));
    }

    @GetMapping("/api/admin/bookings")
    public ResponseEntity<List<BookingDto.BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/api/bookings/{id}")
    public ResponseEntity<BookingDto.BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }
}
