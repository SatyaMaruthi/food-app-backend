package com.foodapp.repository;

import com.foodapp.domain.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    @Query("select s from Seller s where s.status = 'VERIFIED'")
    List<Seller> findVerified();
}
