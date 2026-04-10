package com.foodapp.repository;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findBySellerIdAndStatus(Long sellerId, Enums.PlanStatus status);
    List<SubscriptionPlan> findBySellerId(Long sellerId);
}
