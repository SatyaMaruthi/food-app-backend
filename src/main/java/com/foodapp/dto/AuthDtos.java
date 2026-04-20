package com.foodapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public enum OtpChannel { EMAIL, MOBILE }

    public record RegisterRequest(
            @Email String email,
            @NotBlank String password,
            @NotBlank String fullName,
            @NotBlank String role
    ) {}

    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record OtpRequest(@Email String email, OtpChannel channel, String mobileNumber) {}
    public record VerifyOtpRequest(@Email String email, @NotBlank String otp, String fullName) {}
    public record ForgotPasswordRequest(@Email String email) {}
    public record ResetPasswordRequest(@Email String email, @NotBlank String otp, @NotBlank String newPassword) {}
    public record DeactivateAccountRequest(@Email String email, @NotBlank String otp) {}
    public record OtpResponse(boolean existingUser, String message, String destination) {}
    public record AuthResponse(String token, String email, String role) {}
}
