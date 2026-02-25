package com.bapsdelhibalmandal.balbalika_management_system.Security;

import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhoneNumberAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PhoneAuthenticationToken token = (PhoneAuthenticationToken) authentication;
        String phone = token.getPrincipal().toString();
        String otp = token.getCredentials().toString();

        // TODO: you should verify OTP here – for now, we accept any OTP
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toSet());

        PhoneAuthenticationToken auth = new PhoneAuthenticationToken(phone, authorities);
        auth.setDetails(user);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PhoneAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
