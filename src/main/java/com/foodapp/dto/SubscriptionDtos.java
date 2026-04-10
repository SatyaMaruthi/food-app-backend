package com.foodapp.dto;

import java.time.LocalDate;
import java.util.List;

public class SubscriptionDtos {
    public record CreateSubscriptionRequest(Long sellerId, Long planId, LocalDate startDate) {}
    public record DeliveryActionRequest(LocalDate date) {}
    public record SubscriptionResponse(Long subscriptionId, String status, List<LocalDate> deliveries) {}
}
