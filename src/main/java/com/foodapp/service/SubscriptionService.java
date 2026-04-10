package com.foodapp.service;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.*;
import com.foodapp.dto.SubscriptionDtos;
import com.foodapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionDeliveryRepository deliveryRepository;

    @Transactional
    public SubscriptionDtos.SubscriptionResponse create(Long userId, SubscriptionDtos.CreateSubscriptionRequest request) {
        if (subscriptionRepository.existsByUserIdAndPlanIdAndStatusIn(userId, request.planId(),
                List.of(Enums.SubscriptionStatus.ACTIVE, Enums.SubscriptionStatus.PAUSED))) {
            throw new IllegalStateException("You already have this plan active");
        }
        User user = userRepository.findById(userId).orElseThrow();
        Seller seller = sellerRepository.findById(request.sellerId()).orElseThrow();
        SubscriptionPlan plan = planRepository.findById(request.planId()).orElseThrow();
        Subscription s = new Subscription();
        s.setUser(user);
        s.setSeller(seller);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(request.startDate().plusDays(plan.getDurationDays() - 1L));
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(s);

        LocalDate current = s.getStartDate();
        while (!current.isAfter(s.getEndDate())) {
            SubscriptionDelivery d = new SubscriptionDelivery();
            d.setSubscription(s);
            d.setDeliveryDate(current);
            d.setStatus(Enums.DeliveryStatus.PENDING);
            deliveryRepository.save(d);
            current = current.plusDays(1);
        }

        List<LocalDate> dates = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId())
                .stream().map(SubscriptionDelivery::getDeliveryDate).toList();
        return new SubscriptionDtos.SubscriptionResponse(s.getId(), s.getStatus().name(), dates);
    }

    @Transactional
    public void pause(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        s.setStatus(Enums.SubscriptionStatus.PAUSED);
    }

    @Transactional
    public void resume(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
    }

    @Transactional
    public void cancel(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        s.setStatus(Enums.SubscriptionStatus.CANCELLED);
    }

    @Transactional
    public void skipDay(Long subscriptionId, LocalDate date) {
        SubscriptionDelivery d = deliveryRepository.findBySubscriptionIdAndDeliveryDate(subscriptionId, date).orElseThrow();
        d.setStatus(Enums.DeliveryStatus.SKIPPED);
    }

    public List<SubscriptionDtos.SubscriptionResponse> byUser(Long userId) {
        return subscriptionRepository.findByUserId(userId).stream().map(s -> {
            List<LocalDate> dates = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId())
                    .stream().map(SubscriptionDelivery::getDeliveryDate).toList();
            return new SubscriptionDtos.SubscriptionResponse(s.getId(), s.getStatus().name(), dates);
        }).toList();
    }
}
