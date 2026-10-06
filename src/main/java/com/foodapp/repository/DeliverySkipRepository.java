package com.foodapp.repository;

import com.foodapp.domain.entity.DeliverySkip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliverySkipRepository extends JpaRepository<DeliverySkip, Long> {
    void deleteByScheduledDeliveryId(Long scheduledDeliveryId);
}
