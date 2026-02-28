package com.bapsdelhibalmandal.balbalika_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP storage and verification.
 * For production, consider Redis or a database-backed store.
 */
@Service
public class OtpService {

    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    @Value("${app.otp.expiry-seconds:300}")
    private int expirySeconds;

    /**
     * Generates and stores an OTP for the given phone number.
     * In production, also send via SMS (Twilio, etc.).
     *
     * @param phoneNumber the phone number to send OTP to
     * @return the generated OTP (for dev/testing; in production, don't return it)
     */
    public String generateAndStoreOtp(String phoneNumber) {
        String otp = generateOtp();
        long expiresAt = System.currentTimeMillis() + (expirySeconds * 1000L);
        otpStore.put(phoneNumber, new OtpEntry(otp, expiresAt));
        return otp;
    }

    /**
     * Verifies the OTP for the given phone number.
     *
     * @param phoneNumber the phone number
     * @param otp         the OTP to verify
     * @return true if valid and not expired
     */
    public boolean verifyOtp(String phoneNumber, String otp) {
        OtpEntry entry = otpStore.get(phoneNumber);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expiresAt) {
            otpStore.remove(phoneNumber);
            return false;
        }
        boolean valid = entry.otp.equals(otp);
        if (valid) {
            otpStore.remove(phoneNumber);
        }
        return valid;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(OTP_CHARS.charAt(RANDOM.nextInt(OTP_CHARS.length())));
        }
        return sb.toString();
    }

    private record OtpEntry(String otp, long expiresAt) {}
}
