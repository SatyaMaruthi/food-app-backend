package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "addresses", indexes = @Index(name = "idx_addresses_user", columnList = "user_id"))
public class Address extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private Double latitude;
    private Double longitude;
}
