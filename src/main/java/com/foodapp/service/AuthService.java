package com.foodapp.service;

import com.foodapp.domain.Enums;
import com.foodapp.domain.entity.User;
import com.foodapp.dto.AuthDtos;
import com.foodapp.repository.UserRepository;
import com.foodapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final Map<String, OtpState> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public AuthDtos.OtpResponse requestLoginOtp(AuthDtos.OtpRequest request) {
        boolean existing = userRepository.findByEmail(request.email()).isPresent();
        String otp = generateOtp();
        otpStore.put(request.email().toLowerCase(), new OtpState(otp, Instant.now().plusSeconds(300)));
        AuthDtos.OtpChannel channel = request.channel() == null ? AuthDtos.OtpChannel.EMAIL : request.channel();
        String destination = dispatchOtp(channel, request.email(), request.mobileNumber(), otp);
        String message = existing ? "OTP sent for login" : "OTP sent, complete registration";
        return new AuthDtos.OtpResponse(existing, message, destination);
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
        String otp = generateOtp();
        otpStore.put(user.getEmail().toLowerCase(), new OtpState(otp, Instant.now().plusSeconds(300)));
        String destination = dispatchOtp(AuthDtos.OtpChannel.EMAIL, user.getEmail(), user.getPhone(), otp);
        return new AuthDtos.OtpResponse(true, "Password reset OTP sent", destination);
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

    private String dispatchOtp(AuthDtos.OtpChannel channel, String email, String mobileNumber, String otp) {
        if (channel == AuthDtos.OtpChannel.MOBILE) {
            String masked = maskMobile(mobileNumber);
            log.info("Sending OTP to mobile {}. OTP={}", masked, otp);
            return masked;
        }
        sendOtpEmail(email, otp);
        return maskEmail(email);
    }

    private void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Food App OTP");
            message.setText("Your OTP is " + otp + ". It is valid for 5 minutes.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send OTP email to {}", email, ex);
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at - 1);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) return "provided mobile";
        return "******" + mobile.substring(mobile.length() - 4);
    }

    private record OtpState(String otp, Instant expiresAt) {}
}
