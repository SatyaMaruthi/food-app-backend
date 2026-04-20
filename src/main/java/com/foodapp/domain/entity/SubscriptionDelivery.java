package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import com.foodapp.domain.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "subscription_deliveries", indexes = {
        @Index(name = "idx_deliveries_subscription_date", columnList = "subscription_id,deliveryDate", unique = true),
        @Index(name = "idx_deliveries_status", columnList = "status")
})
public class SubscriptionDelivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    @Column(nullable = false)
    private LocalDate deliveryDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.DeliveryStatus status;
    @Column(length = 120)
    private String deliveryBy;
    @Column(length = 60)
    private String timeZone;
    @Column(length = 20)
    private String mobileNumber;
    @Column(length = 255)
    private String liveTrackingUrl;
    @Column(length = 120)
    private String deliveredBy;
    private LocalDateTime deliveredOn;
    private Integer rating;
    private String feedback;
}
