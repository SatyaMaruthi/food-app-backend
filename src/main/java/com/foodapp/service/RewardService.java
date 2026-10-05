package com.foodapp.service;

import com.foodapp.domain.entity.Reward;
import com.foodapp.domain.entity.User;
import com.foodapp.dto.RewardDtos;
import com.foodapp.repository.RewardRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    @Transactional
    public void initializeReward(Long userId) {
        if (rewardRepository.findByUserId(userId).isPresent()) {
            return;
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Reward reward = new Reward();
        reward.setUser(user);
        reward.setPoints(0);
        reward.setTotalSpent(BigDecimal.ZERO);
        reward.setTier("Silver");
        rewardRepository.save(reward);
        log.info("Initialized reward for user {}", user.getFullName());
    }

    @Transactional
    public void addPoints(Long userId, Integer points) {
        Reward reward = rewardRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found for user"));
        reward.setPoints(reward.getPoints() + points);
        reward.updateTier();
        rewardRepository.save(reward);
        log.info("Added {} points to user {}. Total points: {}, Tier: {}", points, userId, reward.getPoints(), reward.getTier());
    }

    @Transactional
    public void addSpending(Long userId, BigDecimal amount) {
        Reward reward = rewardRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found for user"));
        reward.setTotalSpent(reward.getTotalSpent().add(amount));
        // 1 point per 10 rupees spent
        int pointsEarned = amount.intValue() / 10;
        reward.setPoints(reward.getPoints() + pointsEarned);
        reward.updateTier();
        rewardRepository.save(reward);
        log.info("Added {} spending to user {}. Points earned: {}", amount, userId, pointsEarned);
    }

    public RewardDtos.RewardResponse getRewards(Long userId) {
        Reward reward = rewardRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found for user"));

        int pointsToNextTier = calculatePointsToNextTier(reward.getPoints());

        return new RewardDtos.RewardResponse(
                reward.getPoints(),
                reward.getTier(),
                reward.getTotalSpent(),
                pointsToNextTier
        );
    }

    private int calculatePointsToNextTier(int currentPoints) {
        if (currentPoints < 300) {
            return 300 - currentPoints;  // Points needed to reach Gold
        } else if (currentPoints < 1000) {
            return 1000 - currentPoints;  // Points needed to reach Platinum
        }
        return 0;  // Already at Platinum
    }
}

