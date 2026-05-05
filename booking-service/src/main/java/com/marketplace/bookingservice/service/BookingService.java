package com.marketplace.bookingservice.service;

import com.marketplace.bookingservice.client.OfferServiceClient;
import com.marketplace.bookingservice.client.UserServiceClient;
import com.marketplace.bookingservice.config.AppConfig;
import com.marketplace.bookingservice.dto.BookingDto;
import com.marketplace.bookingservice.entity.Booking;
import com.marketplace.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserServiceClient userServiceClient;
    private final OfferServiceClient offerServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    /**
     * Main booking flow — Saga Pattern via RabbitMQ:
     *
     * Step 1: Fetch customer and offer details (REST)
     * Step 2: Verify offer availability (REST)
     * Step 3: Reserve the offer (REST → offer-service marks BOOKED)
     * Step 4: Deduct wallet (MQ → user-service WalletMessageListener)
     * Step 5a (success): Save CONFIRMED booking → notify both parties (MQ)
     * Step 5b (failure): Rollback — release offer + refund wallet (MQ) → notify customer
     */
    @Transactional
    public BookingDto.BookingResponse createBooking(BookingDto.CreateBookingRequest request) {
        log.info("Processing booking: customerId={}, offerId={}", request.getCustomerId(), request.getOfferId());

        // Step 1: Validate customer
        Map<String, Object> customer = userServiceClient.getUserById(request.getCustomerId());
        if (!"CUSTOMER".equals(customer.get("role"))) {
            throw new IllegalArgumentException("Only customers can make bookings");
        }
        String customerName = (String) customer.get("username");

        // Step 2: Fetch offer
        Map<String, Object> offer = offerServiceClient.getOfferById(request.getOfferId());
        if (!"ACTIVE".equals(offer.get("status"))) {
            throw new IllegalStateException("Offer is not available. Status: " + offer.get("status"));
        }

        BigDecimal price = new BigDecimal(offer.get("price").toString());
        Long providerId = Long.valueOf(offer.get("providerId").toString());
        String providerName = (String) offer.get("providerName");
        String professionType = (String) offer.get("professionType");
        String category = (String) offer.get("category");
        String serviceDate = (String) offer.get("availableDate");

        // Step 3: Reserve the offer (REST call to offer service)
        Map<String, Object> bookOfferBody = Map.of(
                "customerId", request.getCustomerId(),
                "customerName", customerName
        );
        offerServiceClient.bookOffer(request.getOfferId(), bookOfferBody);

        // Step 4: Create PENDING booking record
        Booking booking = Booking.builder()
                .customerId(request.getCustomerId())
                .customerName(customerName)
                .offerId(request.getOfferId())
                .providerId(providerId)
                .providerName(providerName)
                .professionType(professionType)
                .category(category)
                .amount(price)
                .serviceDate(java.time.LocalDate.parse(serviceDate))
                .status(Booking.BookingStatus.PENDING)
                .build();
        booking = bookingRepository.save(booking);
        log.info("Booking {} created in PENDING state", booking.getId());

        // Step 5: Attempt wallet deduction (synchronous request-reply via RabbitMQ)
        String correlationId = UUID.randomUUID().toString();
        String replyQueue = "wallet.reply." + correlationId;

        // Declare the reply queue dynamically so RabbitMQ can route the response to it
        amqpAdmin.declareQueue(new Queue(replyQueue, false, false, true));

        Map<String, Object> deductMsg = new HashMap<>();
        deductMsg.put("userId", request.getCustomerId());
        deductMsg.put("amount", price);
        deductMsg.put("correlationId", correlationId);
        deductMsg.put("replyQueue", replyQueue);

        // Send deduct request
        rabbitTemplate.convertAndSend(AppConfig.BOOKING_EXCHANGE, "wallet.deduct", deductMsg);
        log.info("Wallet deduction request sent for bookingId={}, amount={}", booking.getId(), price);

        // Wait for reply (with timeout)
        Object replyObj = rabbitTemplate.receiveAndConvert(replyQueue, 10000);

        boolean paymentSuccess = false;
        if (replyObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> reply = (Map<String, Object>) replyObj;
            paymentSuccess = Boolean.TRUE.equals(reply.get("success"));
        }

        if (paymentSuccess) {
            // CONFIRM booking
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Mark offer as completed
            offerServiceClient.completeOffer(request.getOfferId());

            // Notify customer asynchronously
            sendNotification("notification.customer." + request.getCustomerId(),
                    buildNotification("BOOKING_CONFIRMED", request.getCustomerId(), customerName,
                            "Your booking #" + booking.getId() + " is CONFIRMED! " +
                            providerName + " will provide " + category + " service on " + serviceDate +
                            ". Amount charged: $" + price, booking.getId()));

            // Notify provider asynchronously
            sendNotification("notification.provider." + providerId,
                    buildNotification("BOOKING_RECEIVED", providerId, providerName,
                            "New booking #" + booking.getId() + " from " + customerName +
                            " for " + category + " on " + serviceDate + ". Amount: $" + price,
                            booking.getId()));

            log.info("Booking {} CONFIRMED successfully", booking.getId());
        } else {
            // ROLLBACK
            booking.setStatus(Booking.BookingStatus.REJECTED);
            booking.setRejectionReason("Insufficient wallet balance");
            bookingRepository.save(booking);

            // Release the offer back to ACTIVE
            offerServiceClient.releaseOffer(request.getOfferId());

            // Refund is not needed since deduction failed, but send notification
            sendNotification("notification.customer." + request.getCustomerId(),
                    buildNotification("BOOKING_REJECTED", request.getCustomerId(), customerName,
                            "Booking rejected: Insufficient wallet balance. " +
                            "Required: $" + price + ". Please top up your wallet and try again.",
                            booking.getId()));

            // Notify admin via DIRECT exchange (PaymentFailed routing key)
            Map<String, Object> adminMsg = new HashMap<>();
            adminMsg.put("event", "PaymentFailed");
            adminMsg.put("bookingId", booking.getId());
            adminMsg.put("customerId", request.getCustomerId());
            adminMsg.put("customerName", customerName);
            adminMsg.put("amount", price);
            adminMsg.put("reason", "Insufficient balance");
            adminMsg.put("timestamp", LocalDateTime.now().toString());
            rabbitTemplate.convertAndSend(AppConfig.PAYMENTS_EXCHANGE, AppConfig.PAYMENT_FAILED_KEY, adminMsg);

            log.warn("Booking {} REJECTED — insufficient balance", booking.getId());
        }

        return BookingDto.BookingResponse.fromEntity(booking);
    }

    private void sendNotification(String routingKey, Map<String, Object> notification) {
        try {
            rabbitTemplate.convertAndSend(AppConfig.NOTIFICATION_EXCHANGE, routingKey, notification);
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", routingKey, e.getMessage());
        }
    }

    private Map<String, Object> buildNotification(String type, Long userId, String username,
                                                   String message, Long bookingId) {
        Map<String, Object> n = new HashMap<>();
        n.put("type", type);
        n.put("userId", userId);
        n.put("username", username);
        n.put("message", message);
        n.put("bookingId", bookingId);
        n.put("timestamp", LocalDateTime.now().toString());
        return n;
    }

    @Transactional(readOnly = true)
    public List<BookingDto.BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(BookingDto.BookingResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto.BookingResponse> getProviderBookings(Long providerId) {
        return bookingRepository.findByProviderId(providerId).stream()
                .map(BookingDto.BookingResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingDto.BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingDto.BookingResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDto.BookingResponse getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(BookingDto.BookingResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
    }
}
