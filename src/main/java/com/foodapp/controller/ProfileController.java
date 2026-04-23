package com.foodapp.controller;

import com.foodapp.dto.RewardDtos;
import com.foodapp.service.FavouriteService;
import com.foodapp.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final RewardService rewardService;
    private final FavouriteService favouriteService;

    @GetMapping("/rewards")
    public RewardDtos.RewardResponse rewards(@RequestHeader("X-User-Id") Long userId) {
        return rewardService.getRewards(userId);
    }
}
