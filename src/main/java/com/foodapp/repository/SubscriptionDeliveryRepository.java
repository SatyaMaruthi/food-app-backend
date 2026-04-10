package com.foodapp.repository;

import com.foodapp.domain.entity.SubscriptionDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionDeliveryRepository extends JpaRepository<SubscriptionDelivery, Long> {
    Optional<SubscriptionDelivery> findBySubscriptionIdAndDeliveryDate(Long subscriptionId, LocalDate deliveryDate);
    List<SubscriptionDelivery> findBySubscriptionIdOrderByDeliveryDateAsc(Long subscriptionId);
}
