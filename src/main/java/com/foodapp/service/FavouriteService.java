package com.foodapp.service;

import com.foodapp.domain.entity.Favourite;
import com.foodapp.domain.entity.Seller;
import com.foodapp.domain.entity.User;
import com.foodapp.dto.FavouriteDtos;
import com.foodapp.repository.FavouriteRepository;
import com.foodapp.repository.SellerRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavouriteService {
    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public void saveFavourite(Long userId, Long sellerId) {
        if (favouriteRepository.findByUserIdAndSellerId(userId, sellerId).isPresent()) {
            log.info("Seller {} already in favourites for user {}", sellerId, userId);
            return;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setSeller(seller);
        favouriteRepository.save(favourite);
        log.info("Saved seller {} to favourites for user {}", seller.getBrandName(), user.getFullName());
    }

    @Transactional
    public void removeFavourite(Long userId, Long sellerId) {
        favouriteRepository.deleteByUserIdAndSellerId(userId, sellerId);
        log.info("Removed seller {} from favourites for user {}", sellerId, userId);
    }

    public List<FavouriteDtos.FavouriteSellerDto> getFavourites(Long userId) {
        return favouriteRepository.findByUserId(userId).stream()
                .map(fav -> new FavouriteDtos.FavouriteSellerDto(
                        fav.getSeller().getId(),
                        fav.getSeller().getBrandName(),
                        "https://placeholder-image.com/" + fav.getSeller().getId(),
                        4.5  // Placeholder rating
                ))
                .toList();
    }

    public boolean isFavourite(Long userId, Long sellerId) {
        return favouriteRepository.findByUserIdAndSellerId(userId, sellerId).isPresent();
    }
}

