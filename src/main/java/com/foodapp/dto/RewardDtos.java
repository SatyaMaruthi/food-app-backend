package com.foodapp.dto;

import java.math.BigDecimal;

public class RewardDtos {
    public record RewardResponse(
            Integer points,
            String tier,
            BigDecimal totalSpent,
            Integer pointsToNextTier
    ) {}

    public record RewardHistoryItem(
            String description,
            Integer pointsEarned,
            String date
    ) {}
}

