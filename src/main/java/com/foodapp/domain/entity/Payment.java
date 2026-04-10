package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import com.foodapp.domain.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_subscription", columnList = "subscription_id"),
        @Index(name = "idx_payments_order_ref", columnList = "providerOrderId", unique = true)
})
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.PaymentProvider provider;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.PaymentStatus status;
    @Column(nullable = false, length = 120)
    private String providerOrderId;
    @Column(length = 120)
    private String providerPaymentId;
}
