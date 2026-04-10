package com.foodapp.service;

import com.foodapp.domain.entity.Seller;
import com.foodapp.domain.entity.SubscriptionPlan;
import com.foodapp.dto.MarketplaceDtos;
import com.foodapp.repository.SellerRepository;
import com.foodapp.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<MarketplaceDtos.SellerCard> nearby(double lat, double lon, int radiusKm) {
        return sellerRepository.findVerified().stream()
                .filter(s -> distanceKm(lat, lon, s.getLatitude(), s.getLongitude()) <= Math.min(radiusKm, s.getDeliveryRadiusKm()))
                .map(s -> new MarketplaceDtos.SellerCard(
                        s.getId(),
                        s.getBrandName(),
                        s.getLatitude(),
                        s.getLongitude(),
                        s.getDeliveryRadiusKm()
                ))
                .toList();
    }

    public MarketplaceDtos.SellerDetail detail(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow();
        List<MarketplaceDtos.PlanDto> plans = subscriptionPlanRepository.findBySellerId(sellerId).stream()
                .map(this::toPlanDto)
                .toList();
        return new MarketplaceDtos.SellerDetail(seller.getId(), seller.getBrandName(), seller.getDeliveryRadiusKm(), plans);
    }

    private MarketplaceDtos.PlanDto toPlanDto(SubscriptionPlan p) {
        return new MarketplaceDtos.PlanDto(p.getId(), p.getName(), p.getPrice(), p.getDurationDays(), p.getStatus().name());
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
