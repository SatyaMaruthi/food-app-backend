package com.foodapp.controller;

import com.foodapp.dto.SubscriptionDtos;
import com.foodapp.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
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
    public void editDelivery(
            @PathVariable Long id,
            @RequestBody SubscriptionDtos.EditDeliveryRequest request
    ) {
        subscriptionService.editActiveDelivery(id, request.date(), request);
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
}
