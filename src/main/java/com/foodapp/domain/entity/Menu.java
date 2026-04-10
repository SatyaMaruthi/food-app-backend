package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import com.foodapp.domain.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menus", indexes = @Index(name = "idx_menus_seller_type", columnList = "seller_id,menuType"))
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.MenuType menuType;
    @Column(nullable = false, length = 120)
    private String title;
}
