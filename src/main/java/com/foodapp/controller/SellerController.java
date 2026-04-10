package com.foodapp.controller;

import com.foodapp.dto.MarketplaceDtos;
import com.foodapp.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;

    @GetMapping("/nearby")
    public List<MarketplaceDtos.SellerCard> nearby(@RequestParam double lat, @RequestParam double lon, @RequestParam(defaultValue = "5") int radiusKm) {
        return sellerService.nearby(lat, lon, radiusKm);
    }

    @GetMapping("/{id}")
    public MarketplaceDtos.SellerDetail detail(@PathVariable Long id) {
        return sellerService.detail(id);
    }
}
