package com.foodapp.controller;

import com.foodapp.dto.FavouriteDtos;
import com.foodapp.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favourites")
@RequiredArgsConstructor
public class FavouriteController {
    private final FavouriteService favouriteService;

    @PostMapping
    public void saveFavourite(@RequestHeader("X-User-Id") Long userId,
                             @RequestBody FavouriteDtos.SaveFavouriteRequest request) {
        favouriteService.saveFavourite(userId, request.sellerId());
    }

    @DeleteMapping("/{sellerId}")
    public void removeFavourite(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long sellerId) {
        favouriteService.removeFavourite(userId, sellerId);
    }

    @GetMapping
    public List<FavouriteDtos.FavouriteSellerDto> getFavourites(@RequestHeader("X-User-Id") Long userId) {
        return favouriteService.getFavourites(userId);
    }

    @GetMapping("/{sellerId}")
    public boolean isFavourite(@RequestHeader("X-User-Id") Long userId,
                              @PathVariable Long sellerId) {
        return favouriteService.isFavourite(userId, sellerId);
    }
}

