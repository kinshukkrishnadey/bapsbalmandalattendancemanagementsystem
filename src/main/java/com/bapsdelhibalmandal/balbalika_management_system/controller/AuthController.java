package com.bapsdelhibalmandal.balbalika_management_system.controller;

import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.OtpService;
import com.bapsdelhibalmandal.balbalika_management_system.service.SmsException;
import com.bapsdelhibalmandal.balbalika_management_system.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final OtpService otpService;
    private final UserRepository userRepository;
    
    @Autowired(required = false)
    private SmsService smsService;

    public AuthController(OtpService otpService, UserRepository userRepository) {
        this.otpService = otpService;
        this.userRepository = userRepository;
    }

    /**
     * Debug: returns current user's roles and rights (requires JWT).
     * Remove in production.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        if (phone == null || phone.equals("anonymousUser")) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        User user = userRepository.findByPhoneNumber(phone).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        var roles = user.getRoles().stream()
                .map(r -> Map.of(
                        "roleId", r.getRoleId(),
                        "roleName", r.getRoleName(),
                        "rights", r.getRights() == null ? java.util.List.of() : r.getRights().stream().map(Enum::name).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("phone", phone, "roles", roles));
    }

    /**
     * Request OTP for the given phone number.
     * OTP is sent via SMS to Indian phone numbers.
     * Phone number must be a valid 10-digit Indian mobile number (starting with 6, 7, 8, or 9).
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String phone) {
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
        }
        
        // Validate Indian phone number if SMS service is available
        if (smsService != null && !smsService.isValidIndianNumber(phone)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", 
                    "Invalid Indian phone number. Please provide a 10-digit mobile number starting with 6, 7, 8, or 9."
            ));
        }
        
        if (!userRepository.existsByPhoneNumber(phone)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for this phone number"));
        }
        
        try {
            String otp = otpService.generateAndStoreOtp(phone);
            
            Map<String, Object> response = Map.of("message", "OTP sent successfully to your registered mobile number");
            
            // Only include OTP in response if explicitly enabled for development/testing
            if (otp != null) {
                return ResponseEntity.ok(Map.of(
                        "message", "OTP sent successfully",
                        "otp", otp  // Only in dev mode when app.otp.return-in-response=true
                ));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (SmsException e) {
            logger.error("Failed to send OTP via SMS: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", 
                    "Failed to send OTP. Please try again later."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error generating OTP: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", 
                    "An error occurred while generating OTP. Please try again."
            ));
        }
    }
}
