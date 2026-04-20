package com.foodapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodAppBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoodAppBackendApplication.class, args);
    }
}
