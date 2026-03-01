package com.bapsdelhibalmandal.balbalika_management_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    
    private static final String OTP_CHARS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private SmsService smsService;

    @Value("${app.otp.expiry-seconds:300}")
    private int expirySeconds;
    
    @Value("${app.otp.return-in-response:false}")
    private boolean returnOtpInResponse;

    /**
     * Generates and stores an OTP for the given phone number.
     * Sends OTP via SMS if SMS service is configured and enabled.
     *
     * @param phoneNumber the phone number to send OTP to (must be valid Indian number)
     * @return the generated OTP (only returned if returnOtpInResponse is true, for dev/testing)
     * @throws SmsException if SMS sending fails
     */
    public String generateAndStoreOtp(String phoneNumber) throws SmsException {
        // Validate phone number if SMS service is available
        if (smsService != null && !smsService.isValidIndianNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid Indian phone number: " + phoneNumber);
        }
        
        String otp = generateOtp();
        long expiresAt = System.currentTimeMillis() + (expirySeconds * 1000L);
        otpStore.put(phoneNumber, new OtpEntry(otp, expiresAt));
        
        // Send OTP via SMS if service is available
        if (smsService != null) {
            try {
                boolean sent = smsService.sendOtp(phoneNumber, otp);
                if (sent) {
                    logger.info("OTP sent successfully via SMS to {}", phoneNumber);
                } else {
                    logger.warn("OTP generated but SMS sending failed for {}", phoneNumber);
                }
            } catch (SmsException e) {
                logger.error("Failed to send OTP via SMS to {}: {}", phoneNumber, e.getMessage());
                // Don't throw - OTP is still stored, just SMS failed
                // In production, you might want to throw or handle differently
            }
        } else {
            logger.warn("SMS service not configured. OTP generated but not sent: {}", otp);
        }
        
        return returnOtpInResponse ? otp : null;
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
