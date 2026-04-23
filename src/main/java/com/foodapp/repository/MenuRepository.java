package com.foodapp.repository;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findBySellerIdAndMenuType(Long sellerId, Enums.MenuType menuType);
}

