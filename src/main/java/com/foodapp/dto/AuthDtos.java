package com.foodapp.dto;

import com.foodapp.enums.OtpPurpose;
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
    public record OtpRequest(String email, String mobileNumber, AuthDtos.OtpChannel channel, OtpPurpose purpose) {}
    public record VerifyOtpRequest(String email, String otp, String fullName, OtpPurpose purpose) {}
    public record ForgotPasswordRequest(@Email String email) {}
    public record ResetPasswordRequest(@Email String email, @NotBlank String otp, @NotBlank String newPassword) {}
    public record DeactivateAccountRequest(@Email String email, @NotBlank String otp) {}
    public record OtpResponse(boolean existingUser, String message, String destination) {}
    public record AuthResponse(String token, Long userId, String email, String role) {}
}
