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

    @GetMapping
    public List<SubscriptionDtos.SubscriptionResponse> list(@RequestHeader("X-User-Id") Long userId) {
        return subscriptionService.byUser(userId);
    }
}
