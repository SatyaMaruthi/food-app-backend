package com.foodapp.config;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.Seller;
import com.foodapp.domain.entity.SubscriptionPlan;
import com.foodapp.domain.entity.User;
import com.foodapp.repository.SellerRepository;
import com.foodapp.repository.SubscriptionPlanRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            if (userRepository.findByEmail("user@test.com").isEmpty()) {
                User user = new User();
                user.setEmail("user@test.com");
                user.setPasswordHash(passwordEncoder.encode("pass123"));
                user.setFullName("Demo User");
                user.setRole(Enums.Role.USER);
                user.setLatitude(12.9716);
                user.setLongitude(77.5946);
                userRepository.save(user);
            }

            if (userRepository.findByEmail("seller@test.com").isEmpty()) {
                User sellerUser = new User();
                sellerUser.setEmail("seller@test.com");
                sellerUser.setPasswordHash(passwordEncoder.encode("pass123"));
                sellerUser.setFullName("Home Kitchen Seller");
                sellerUser.setRole(Enums.Role.SELLER);
                sellerUser = userRepository.save(sellerUser);

                Seller seller = new Seller();
                seller.setUser(sellerUser);
                seller.setBrandName("Healthy Home Bowls");
                seller.setLatitude(12.9721);
                seller.setLongitude(77.5933);
                seller.setDeliveryRadiusKm(8);
                seller.setStatus(Enums.SellerStatus.VERIFIED);
                seller = sellerRepository.save(seller);

                SubscriptionPlan daily = new SubscriptionPlan();
                daily.setSeller(seller);
                daily.setName("Daily Meal Plan");
                daily.setPrice(new BigDecimal("149.00"));
                daily.setDurationDays(30);
                daily.setStatus(Enums.PlanStatus.ACTIVE);
                subscriptionPlanRepository.save(daily);

                SubscriptionPlan fruits = new SubscriptionPlan();
                fruits.setSeller(seller);
                fruits.setName("Fruit Bowl Subscription");
                fruits.setPrice(new BigDecimal("99.00"));
                fruits.setDurationDays(30);
                fruits.setStatus(Enums.PlanStatus.ACTIVE);
                subscriptionPlanRepository.save(fruits);
            }
        };
    }
}
