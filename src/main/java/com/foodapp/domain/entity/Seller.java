package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import com.foodapp.domain.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sellers", indexes = {
        @Index(name = "idx_sellers_user", columnList = "user_id", unique = true),
        @Index(name = "idx_sellers_status", columnList = "status"),
        @Index(name = "idx_sellers_geo", columnList = "latitude, longitude")
})
public class Seller extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 160)
    private String brandName;
    private Double latitude;
    private Double longitude;
    @Column(nullable = false)
    private Integer deliveryRadiusKm;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.SellerStatus status;
}
