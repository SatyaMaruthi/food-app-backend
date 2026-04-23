package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "rewards", indexes = @Index(name = "idx_rewards_user", columnList = "user_id"))
public class Reward extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(length = 50)
    private String tier = "Silver";

    // Calculate based on points: Silver (0-299), Gold (300-999), Platinum (1000+)
    public void updateTier() {
        if (points >= 1000) {
            this.tier = "Platinum";
        } else if (points >= 300) {
            this.tier = "Gold";
        } else {
            this.tier = "Silver";
        }
    }
}

