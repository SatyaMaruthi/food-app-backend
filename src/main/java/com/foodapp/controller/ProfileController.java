package com.foodapp.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final Map<Long, Integer> rewardsByUser = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> favoritesByUser = new ConcurrentHashMap<>();

    @GetMapping("/rewards")
    public Map<String, Object> rewards(@RequestHeader("X-User-Id") Long userId) {
        int points = rewardsByUser.getOrDefault(userId, 120);
        return Map.of("points", points, "tier", points >= 300 ? "Gold" : "Silver");
    }

    @GetMapping("/favorites")
    public List<String> favorites(@RequestHeader("X-User-Id") Long userId) {
        return favoritesByUser.getOrDefault(userId, List.of());
    }

    @PostMapping("/favorites")
    public void saveFavorite(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "").trim();
        if (name.isBlank()) {
            return;
        }
        favoritesByUser.merge(userId, new java.util.ArrayList<>(List.of(name)), (oldValue, ignored) -> {
            if (!oldValue.contains(name)) {
                oldValue.add(name);
            }
            return oldValue;
        });
    }
}
