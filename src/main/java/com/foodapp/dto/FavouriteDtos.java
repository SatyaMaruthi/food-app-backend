package com.foodapp.dto;

import java.util.List;

public class FavouriteDtos {
    public record FavouriteSellerDto(
            Long sellerId,
            String brandName,
            String imageUrl,
            Double rating
    ) {}

    public record SaveFavouriteRequest(Long sellerId) {}

    public record FavouriteListResponse(
            List<FavouriteSellerDto> favourites
    ) {}
}

