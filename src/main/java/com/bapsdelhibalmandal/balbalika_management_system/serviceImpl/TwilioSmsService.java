package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.service.SmsException;
import com.bapsdelhibalmandal.balbalika_management_system.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.regex.Pattern;

/**
 * Twilio SMS service implementation for sending OTP to Indian phone numbers.
 * 
 * Twilio is a popular international SMS gateway with good support for India.
 * 
 * Setup:
 * 1. Sign up at https://www.twilio.com/
 * 2. Get your Account SID and Auth Token from dashboard
 * 3. Get a Twilio phone number (supports Indian numbers)
 * 4. Verify your phone number for testing (free tier)
 * 
 * Configuration:
 * - sms.provider.account-sid: Your Twilio Account SID
 * - sms.provider.auth-token: Your Twilio Auth Token
 * - sms.provider.from-number: Your Twilio phone number (E.164 format: +91XXXXXXXXXX or +1XXXXXXXXXX)
 */
@Service
public class TwilioSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);
    
    // Indian mobile number pattern: 10 digits starting with 6, 7, 8, or 9
    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    
    @Value("${sms.provider.account-sid:}")
    private String accountSid;
    
    @Value("${sms.provider.auth-token:}")
    private String authToken;
    
    @Value("${sms.provider.from-number:}")
    private String fromNumber;
    
    @Value("${sms.provider.enabled:false}")
    private boolean smsEnabled;
    
    @PostConstruct
    public void init() {
        if (smsEnabled && accountSid != null && !accountSid.isEmpty() && 
            authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio SMS service initialized");
        } else {
            logger.warn("Twilio SMS service is disabled or not configured");
        }
    }
    
    @Override
    public boolean sendOtp(String phoneNumber, String otp) throws SmsException {
        if (!smsEnabled) {
            logger.warn("SMS service is disabled. OTP not sent to {}", phoneNumber);
            return false;
        }
        
        if (!isValidIndianNumber(phoneNumber)) {
            throw new SmsException("Invalid Indian phone number: " + phoneNumber);
        }
        
        if (accountSid == null || accountSid.isEmpty()) {
            throw new SmsException("Twilio Account SID is not configured");
        }
        
        if (authToken == null || authToken.isEmpty()) {
            throw new SmsException("Twilio Auth Token is not configured");
        }
        
        if (fromNumber == null || fromNumber.isEmpty()) {
            throw new SmsException("Twilio From Number is not configured");
        }
        
        try {
            String formattedNumber = formatIndianNumber(phoneNumber);
            String messageBody = "Your OTP is " + otp + ". Valid for 5 minutes. Do not share with anyone.";
            
            logger.info("Sending OTP via Twilio to {}", formattedNumber);
            
            Message message = Message.creator(
                    new PhoneNumber(formattedNumber),  // To
                    new PhoneNumber(fromNumber),        // From
                    messageBody                         // Message
            ).create();
            
            String messageStatus = message.getStatus().toString();
            logger.info("Twilio message sent. Status: {}, SID: {}", messageStatus, message.getSid());
            
            // Check if message was successfully queued/sent
            if ("queued".equalsIgnoreCase(messageStatus) || 
                "sent".equalsIgnoreCase(messageStatus) ||
                "sending".equalsIgnoreCase(messageStatus)) {
                return true;
            } else {
                logger.warn("Twilio message status: {}", messageStatus);
                // Still return true as message was accepted by Twilio
                return true;
            }
            
        } catch (com.twilio.exception.ApiException e) {
            logger.error("Twilio API error sending OTP to {}: {}", phoneNumber, e.getMessage(), e);
            throw new SmsException("Failed to send OTP via Twilio: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error sending OTP via Twilio to {}: {}", phoneNumber, e.getMessage(), e);
            throw new SmsException("Error sending OTP: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isValidIndianNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }
        
        // Remove spaces, dashes, and country code if present
        String cleaned = phoneNumber.replaceAll("[\\s\\-+]", "");
        
        // Remove country code if present (+91 or 91)
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("+91") && cleaned.length() == 13) {
            cleaned = cleaned.substring(3);
        }
        
        // Check if it's a valid 10-digit Indian mobile number
        return INDIAN_MOBILE_PATTERN.matcher(cleaned).matches();
    }
    
    @Override
    public String formatIndianNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }
        
        // Remove spaces, dashes
        String cleaned = phoneNumber.replaceAll("[\\s\\-+]", "");
        
        // Remove country code if present
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("+91") && cleaned.length() == 13) {
            cleaned = cleaned.substring(3);
        }
        
        // Validate and format to E.164 format
        if (INDIAN_MOBILE_PATTERN.matcher(cleaned).matches()) {
            return "+91" + cleaned;
        }
        
        // Return original if invalid
        return phoneNumber;
    }
}
