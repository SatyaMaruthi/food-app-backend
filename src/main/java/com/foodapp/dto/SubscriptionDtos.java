package com.foodapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionDtos {
    public record MenuItem(
            Long id,
            String name,
            String description,
            String price
    ) {}

    public record WeeklyMenu(
            Long id,
            String title,
            List<MenuItem> items
    ) {}

    public record CreateSubscriptionRequest(Long sellerId, Long planId, LocalDate startDate) {}
    public record DeliveryActionRequest(LocalDate date, Integer rating, String feedback) {}
    public record EditDeliveryRequest(
            LocalDate date,
            String timeZone,
            String mobileNumber
    ) {}
    public record DeliverySummary(
            LocalDate date,
            String status,
            String deliveredBy,
            LocalDateTime deliveredOn,
            Integer rating,
            String feedback,
            String deliveryBy,
            String timeZone,
            String mobileNumber,
            String liveTrackingUrl
    ) {}
    public record SubscriptionResponse(
            Long subscriptionId,
            String status,
            String sellerName,
            String planName,
            WeeklyMenu weeklyMenu,
            String weeklyAmount,
            List<DeliverySummary> deliveries
    ) {}
}


