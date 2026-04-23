package com.foodapp.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(name = "twilio.account-sid", havingValue = "your-account-sid", matchIfMissing = false)
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialized");
    }

    public void sendSms(String toPhoneNumber, String message) {
        try {
            // Ensure phone number has country code
            String formattedNumber = formatPhoneNumber(toPhoneNumber);

            Message sms = Message.creator(
                    new PhoneNumber(formattedNumber),
                    new PhoneNumber(fromPhoneNumber),
                    message
            ).create();

            log.info("SMS sent successfully to {} with SID: {}", maskPhoneNumber(toPhoneNumber), sms.getSid());
        } catch (Exception ex) {
            log.error("Failed to send SMS to {}: {}", maskPhoneNumber(toPhoneNumber), ex.getMessage());
            throw new RuntimeException("SMS sending failed", ex);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any spaces, dashes, or parentheses
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // If it doesn't start with +, assume it's Indian number and add +91
        if (!cleaned.startsWith("+")) {
            cleaned = "+91" + cleaned;
        }

        return cleaned;
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
