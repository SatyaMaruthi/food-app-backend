package com.foodapp.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "delivery_skip")
public class DeliverySkip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "scheduled_delivery_id", nullable = false)
    private SubscriptionDelivery scheduledDelivery;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 512)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}