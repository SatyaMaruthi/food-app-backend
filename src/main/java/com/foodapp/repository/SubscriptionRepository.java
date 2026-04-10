package com.foodapp.repository;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    boolean existsByUserIdAndPlanIdAndStatusIn(Long userId, Long planId, List<Enums.SubscriptionStatus> statuses);
}
