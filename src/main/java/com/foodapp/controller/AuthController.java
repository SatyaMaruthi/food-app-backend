package com.foodapp.controller;

import com.foodapp.dto.AuthDtos;
import com.foodapp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/login/otp/request")
    public AuthDtos.OtpResponse requestOtp(@Valid @RequestBody AuthDtos.OtpRequest request) {
        return authService.requestLoginOtp(request);
    }

    @PostMapping("/login/otp/verify")
    public AuthDtos.AuthResponse verifyOtp(@Valid @RequestBody AuthDtos.VerifyOtpRequest request) {
        return authService.verifyLoginOtp(request);
    }

    @PostMapping("/forgot-password")
    public AuthDtos.OtpResponse forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/forgot-password/reset")
    public void resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @PostMapping("/deactivate")
    public void deactivate(@Valid @RequestBody AuthDtos.DeactivateAccountRequest request) {
        authService.deactivate(request);
    }

    @PostMapping("/logout")
    public void logout() {
        // Stateless JWT logout is handled client-side by token removal.
    }
}
