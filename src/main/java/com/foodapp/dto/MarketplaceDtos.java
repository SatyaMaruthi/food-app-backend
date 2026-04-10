package com.foodapp.dto;

import java.math.BigDecimal;
import java.util.List;

public class MarketplaceDtos {
    public record SellerCard(Long id, String brandName, Double latitude, Double longitude, Integer deliveryRadiusKm) {}
    public record PlanDto(Long id, String name, BigDecimal price, Integer durationDays, String status) {}
    public record SellerDetail(Long id, String brandName, Integer deliveryRadiusKm, List<PlanDto> plans) {}
}
