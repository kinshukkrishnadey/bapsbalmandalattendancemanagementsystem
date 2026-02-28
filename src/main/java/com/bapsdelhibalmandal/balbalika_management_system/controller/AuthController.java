package com.bapsdelhibalmandal.balbalika_management_system.controller;

import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import com.bapsdelhibalmandal.balbalika_management_system.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final UserRepository userRepository;

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
     * In production, the OTP would be sent via SMS - it should NOT be returned in the response.
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String phone) {
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
        }
        if (!userRepository.existsByPhoneNumber(phone)) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found for this phone number"));
        }
        String otp = otpService.generateAndStoreOtp(phone);
        // TODO: Send OTP via SMS (Twilio, AWS SNS, etc.) - do NOT return in production
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "otp", otp  // Remove in production - for dev/testing only
        ));
    }
}
