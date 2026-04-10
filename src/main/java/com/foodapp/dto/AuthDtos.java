package com.foodapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record RegisterRequest(
            @Email String email,
            @NotBlank String password,
            @NotBlank String fullName,
            @NotBlank String role
    ) {}

    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String email, String role) {}
}
