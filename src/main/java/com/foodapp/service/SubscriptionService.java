package com.foodapp.service;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.*;
import com.foodapp.dto.SubscriptionDtos;
import com.foodapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
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
        log.info("Creating subscription for user {} with plan {} from seller {}", user.getFullName(), plan.getName(), seller.getBrandName());
        Subscription s = new Subscription();
        s.setUser(user);
        s.setSeller(seller);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(request.startDate().plusDays(plan.getDurationDays() - 1L));
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
        log.info("Subscription details: startDate={}, endDate={}", s.getStartDate(), s.getEndDate());
        subscriptionRepository.save(s);

        LocalDate current = s.getStartDate();
        while (!current.isAfter(s.getEndDate())) {
            SubscriptionDelivery d = new SubscriptionDelivery();
            d.setSubscription(s);
            d.setDeliveryDate(current);
            d.setStatus(Enums.DeliveryStatus.PENDING);
            deliveryRepository.save(d);
            current = current.plusDays(1);
            log.info("Scheduled delivery for date {}", d.getDeliveryDate());
        }

        List<LocalDate> dates = deliveryRepository.findBySubscriptionIdOrderByDeliveryDateAsc(s.getId())
                .stream().map(SubscriptionDelivery::getDeliveryDate).toList();
        log.info("Created subscription with ID {} and delivery dates: {}", s.getId(), dates);
        return new SubscriptionDtos.SubscriptionResponse(s.getId(), s.getStatus().name(), dates);
    }

    @Transactional
    public void pause(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Pausing subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
        s.setStatus(Enums.SubscriptionStatus.PAUSED);
    }

    @Transactional
    public void resume(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Resuming subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
        s.setStatus(Enums.SubscriptionStatus.ACTIVE);
    }

    @Transactional
    public void cancel(Long subscriptionId) {
        Subscription s = subscriptionRepository.findById(subscriptionId).orElseThrow();
        log.info("Cancelling subscription with ID {}. Current status: {}", s.getId(), s.getStatus());
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
