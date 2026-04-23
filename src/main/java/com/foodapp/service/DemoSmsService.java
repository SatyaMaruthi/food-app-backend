package com.foodapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "twilio.account-sid", havingValue = "your-account-sid", matchIfMissing = true)
public class DemoSmsService {

    public void sendSms(String toPhoneNumber, String message) {
        // Extract OTP from message for demo purposes
        String otp = extractOtpFromMessage(message);
        log.info("DEMO SMS: Would send OTP '{}' to {} | Message: {}", otp, maskPhoneNumber(toPhoneNumber), message);
        log.info("For testing, use OTP: {}", otp);
    }

    private String extractOtpFromMessage(String message) {
        // Extract 6-digit OTP from message
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return "123456"; // fallback
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "invalid";
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        if (cleaned.length() < 4) {
            return "invalid";
        }

        return "******" + cleaned.substring(cleaned.length() - 4);
    }
}
