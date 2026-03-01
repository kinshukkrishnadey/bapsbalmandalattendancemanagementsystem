package com.bapsdelhibalmandal.balbalika_management_system.service;

/**
 * Exception thrown when SMS sending fails.
 */
public class SmsException extends Exception {
    
    public SmsException(String message) {
        super(message);
    }
    
    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
