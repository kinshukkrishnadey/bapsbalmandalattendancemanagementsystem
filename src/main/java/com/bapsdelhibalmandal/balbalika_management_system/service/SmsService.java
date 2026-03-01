package com.bapsdelhibalmandal.balbalika_management_system.service;

/**
 * Service interface for sending SMS messages.
 * Implementations can use different SMS providers (MSG91, Twilio, etc.)
 */
public interface SmsService {
    
    /**
     * Sends an OTP SMS to the given phone number.
     * 
     * @param phoneNumber The phone number (should be validated as Indian number)
     * @param otp The OTP to send
     * @return true if SMS was sent successfully, false otherwise
     * @throws SmsException if there's an error sending the SMS
     */
    boolean sendOtp(String phoneNumber, String otp) throws SmsException;
    
    /**
     * Validates if the phone number is a valid Indian mobile number.
     * Indian mobile numbers: 10 digits starting with 6, 7, 8, or 9
     * 
     * @param phoneNumber The phone number to validate
     * @return true if valid Indian number, false otherwise
     */
    boolean isValidIndianNumber(String phoneNumber);
    
    /**
     * Formats Indian phone number to standard format (with country code).
     * Input: 9876543210 -> Output: +919876543210
     * 
     * @param phoneNumber The phone number to format
     * @return Formatted phone number with country code, or original if invalid
     */
    String formatIndianNumber(String phoneNumber);
}
