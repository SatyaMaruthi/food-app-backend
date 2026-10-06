package com.foodapp.controller;

import com.foodapp.dto.SubscriptionDtos;
import com.foodapp.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public SubscriptionDtos.SubscriptionResponse create(@RequestHeader("X-User-Id") Long userId,
                                                        @RequestBody SubscriptionDtos.CreateSubscriptionRequest request) {
        return subscriptionService.create(userId, request);
    }

    @PatchMapping("/{id}/pause")
    public void pause(@PathVariable Long id) { subscriptionService.pause(id); }

    @PatchMapping("/{id}/resume")
    public void resume(@PathVariable Long id) { subscriptionService.resume(id); }

    @PatchMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id) { subscriptionService.cancel(id); }

    @PatchMapping("/{id}/skip")
    public void skip(@PathVariable Long id, @RequestBody SubscriptionDtos.DeliveryActionRequest request) {
        subscriptionService.skipDay(id, request.date());
    }

    @PatchMapping("/{id}/deliver")
    public void markDelivered(@PathVariable Long id, @RequestBody SubscriptionDtos.DeliveryActionRequest request) {
        subscriptionService.markDelivered(id, request);
    }

    @PatchMapping("/{id}/edit-delivery")
    public ResponseEntity<?> editDelivery(@PathVariable Long id, @RequestBody SubscriptionDtos.EditDeliveryRequest request) {
        try {
            subscriptionService.editActiveDelivery(id, request);
            return ResponseEntity.ok(Map.of("message", "Delivery updated"));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found", ex);
        } catch (Exception ex) {
            log.error("Failed to edit delivery", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not edit delivery", ex);
        }
    }


    @GetMapping
    public List<SubscriptionDtos.SubscriptionResponse> list(@RequestHeader("X-User-Id") Long userId) {
        return subscriptionService.byUser(userId);
    }

    @GetMapping("/active-orders")
    public List<SubscriptionDtos.SubscriptionResponse> activeOrders(@RequestHeader("X-User-Id") Long userId) {
        return subscriptionService.activeOrders(userId);
    }

    @GetMapping("/past-orders")
    public List<SubscriptionDtos.SubscriptionResponse> pastOrders(@RequestHeader("X-User-Id") Long userId) {
        return subscriptionService.pastOrders(userId);
    }

    @PostMapping("/{subscriptionId}/deliveries/{deliveryId}/skip")
    public ResponseEntity<?> skipDelivery(
            @PathVariable Long subscriptionId,
            @PathVariable Long deliveryId,
            @RequestBody SkipRequest body,
            @RequestHeader("X-User-Id") Long userId) {

        subscriptionService.skipDelivery(userId, subscriptionId, deliveryId, body.reason());
        return ResponseEntity.ok(Map.of("message", "Delivery skipped", "deliveryId", deliveryId));
    }

    @PostMapping("/{subscriptionId}/deliveries/{deliveryId}/unskip")
    public ResponseEntity<?> unskipDelivery(
            @PathVariable Long subscriptionId,
            @PathVariable Long deliveryId,
            @RequestHeader("X-User-Id") Long userId) {

        subscriptionService.unskipDelivery(userId, subscriptionId, deliveryId);
        return ResponseEntity.ok(Map.of("message", "Skip undone", "deliveryId", deliveryId));
    }

    public static record SkipRequest(String reason) {}
}
