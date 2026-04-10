package com.foodapp.controller;

import com.foodapp.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @PostMapping("/initiate")
    public ApiResponse<String> initiate(@RequestParam String provider) {
        // Integrate Stripe/Razorpay SDK here.
        return ApiResponse.ok("Payment initiated with " + provider);
    }

    @PostMapping("/webhook")
    public ApiResponse<String> webhook() {
        return ApiResponse.ok("Webhook received");
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<String> refund(@PathVariable Long id) {
        return ApiResponse.ok("Refund requested for payment " + id);
    }
}
