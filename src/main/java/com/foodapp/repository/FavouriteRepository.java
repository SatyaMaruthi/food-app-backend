package com.foodapp.repository;

import com.foodapp.domain.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    List<Favourite> findByUserId(Long userId);
    Optional<Favourite> findByUserIdAndSellerId(Long userId, Long sellerId);
    void deleteByUserIdAndSellerId(Long userId, Long sellerId);
}

