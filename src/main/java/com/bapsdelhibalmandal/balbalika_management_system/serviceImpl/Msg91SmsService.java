package com.bapsdelhibalmandal.balbalika_management_system.serviceImpl;

import com.bapsdelhibalmandal.balbalika_management_system.service.SmsException;
import com.bapsdelhibalmandal.balbalika_management_system.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * MSG91 SMS service implementation for sending OTP to Indian phone numbers.
 * 
 * MSG91 is a popular SMS gateway provider in India with good delivery rates.
 * 
 * NOTE: This service is available but not active by default.
 * The default SMS provider is Twilio (TwilioSmsService).
 * 
 * To use MSG91 instead of Twilio:
 * 1. Remove @Service annotation from TwilioSmsService
 * 2. Keep @Service annotation on this class
 * 3. Configure MSG91 credentials in application.properties
 * 
 * Setup:
 * 1. Sign up at https://msg91.com/
 * 2. Get your API key from dashboard
 * 3. Configure sender ID (6 characters, alphanumeric, approved by TRAI)
 * 4. Set up DLT template (required for India)
 * 
 * Configuration:
 * - sms.provider.api-key: Your MSG91 API key
 * - sms.provider.sender-id: Your approved sender ID
 * - sms.provider.template-id: Your DLT template ID (optional, can use default)
 */
// @Service  // Disabled - Twilio is the default SMS provider
// To use MSG91 instead: uncomment @Service above and comment @Service in TwilioSmsService
public class Msg91SmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(Msg91SmsService.class);
    
    // Indian mobile number pattern: 10 digits starting with 6, 7, 8, or 9
    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    
    private final RestTemplate restTemplate;
    
    @Value("${sms.provider.api-key:}")
    private String apiKey;
    
    @Value("${sms.provider.sender-id:}")
    private String senderId;
    
    @Value("${sms.provider.template-id:}")
    private String templateId;
    
    @Value("${sms.provider.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${sms.provider.url:https://control.msg91.com/api/sendotp.php}")
    private String apiUrl;
    
    public Msg91SmsService() {
        this.restTemplate = new RestTemplate();
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
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new SmsException("MSG91 API key is not configured");
        }
        
        if (senderId == null || senderId.isEmpty()) {
            throw new SmsException("MSG91 Sender ID is not configured");
        }
        
        try {
            String formattedNumber = formatIndianNumber(phoneNumber);
            // Remove +91 prefix for MSG91 API (it expects just the 10-digit number)
            String mobileNumber = formattedNumber.replace("+91", "");
            
            // MSG91 SMS API - using sendotp.php endpoint (GET request)
            // Build URL with query parameters (URL encoded)
            String message = "Your OTP is " + otp + ". Valid for 5 minutes. Do not share with anyone.";
            StringBuilder urlBuilder = new StringBuilder(apiUrl);
            urlBuilder.append("?authkey=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
            urlBuilder.append("&mobile=").append(URLEncoder.encode(mobileNumber, StandardCharsets.UTF_8));
            urlBuilder.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
            urlBuilder.append("&sender=").append(URLEncoder.encode(senderId, StandardCharsets.UTF_8));
            if (templateId != null && !templateId.isEmpty()) {
                urlBuilder.append("&DLT_TE_ID=").append(URLEncoder.encode(templateId, StandardCharsets.UTF_8));
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            logger.info("Sending OTP via MSG91 to {}", formattedNumber);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    request,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                logger.debug("MSG91 API response: {}", responseBody);
                
                // MSG91 returns JSON like: {"type":"success","message":"OTP sent successfully"}
                // or error: {"type":"error","message":"..."}
                if (responseBody != null && responseBody.contains("\"type\":\"success\"")) {
                    logger.info("OTP sent successfully to {}", formattedNumber);
                    return true;
                } else {
                    logger.error("MSG91 API returned error: {}", responseBody);
                    throw new SmsException("Failed to send OTP. Please check MSG91 configuration.");
                }
            } else {
                logger.error("MSG91 API returned status: {}", response.getStatusCode());
                throw new SmsException("Failed to send OTP: HTTP " + response.getStatusCode());
            }
            
        } catch (SmsException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error sending OTP via MSG91 to {}: {}", phoneNumber, e.getMessage(), e);
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
        
        // Validate and format
        if (INDIAN_MOBILE_PATTERN.matcher(cleaned).matches()) {
            return "+91" + cleaned;
        }
        
        // Return original if invalid
        return phoneNumber;
    }
}
