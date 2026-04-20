package com.foodapp.domain.entity;

import com.foodapp.common.BaseEntity;
import com.foodapp.domain.Enums;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false, length = 120)
    private String fullName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Enums.Role role;
    @Column(length = 20)
    private String phone;
    @Column(nullable = false)
    private boolean active = true;
    private Double latitude;
    private Double longitude;
}
