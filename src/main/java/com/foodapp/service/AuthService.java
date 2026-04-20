package com.foodapp.service;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.User;
import com.foodapp.dto.AuthDtos;
import com.foodapp.repository.UserRepository;
import com.foodapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Map<String, OtpState> otpStore = new ConcurrentHashMap<>();

    public AuthDtos.OtpResponse requestLoginOtp(AuthDtos.OtpRequest request) {
        boolean existing = userRepository.findByEmail(request.email()).isPresent();
        otpStore.put(request.email().toLowerCase(), new OtpState("123456", Instant.now().plusSeconds(300)));
        String message = existing ? "OTP sent for login" : "OTP sent, complete registration";
        return new AuthDtos.OtpResponse(existing, message);
    }

    public AuthDtos.AuthResponse verifyLoginOtp(AuthDtos.VerifyOtpRequest request) {
        String email = request.email().toLowerCase();
        OtpState otpState = otpStore.get(email);
        if (otpState == null || otpState.expiresAt().isBefore(Instant.now()) || !otpState.otp().equals(request.otp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerOtpUser(email, request.fullName()));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        otpStore.remove(email);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Enums.Role.valueOf(request.role().toUpperCase()));
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthDtos.AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthDtos.OtpResponse forgotPassword(AuthDtos.ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        otpStore.put(user.getEmail().toLowerCase(), new OtpState("654321", Instant.now().plusSeconds(300)));
        return new AuthDtos.OtpResponse(true, "Password reset OTP sent");
    }

    public void resetPassword(AuthDtos.ResetPasswordRequest request) {
        String email = request.email().toLowerCase();
        OtpState otpState = otpStore.get(email);
        if (otpState == null || otpState.expiresAt().isBefore(Instant.now()) || !otpState.otp().equals(request.otp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        otpStore.remove(email);
    }

    public void deactivate(AuthDtos.DeactivateAccountRequest request) {
        String email = request.email().toLowerCase();
        OtpState otpState = otpStore.get(email);
        if (otpState == null || otpState.expiresAt().isBefore(Instant.now()) || !otpState.otp().equals(request.otp())) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
        otpStore.remove(email);
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredOtps() {
        Instant now = Instant.now();
        otpStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private User registerOtpUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setFullName((fullName == null || fullName.isBlank()) ? "New User" : fullName);
        user.setPasswordHash(passwordEncoder.encode("OTP_ONLY_USER"));
        user.setRole(Enums.Role.USER);
        user.setActive(true);
        return userRepository.save(user);
    }

    private record OtpState(String otp, Instant expiresAt) {}
}
