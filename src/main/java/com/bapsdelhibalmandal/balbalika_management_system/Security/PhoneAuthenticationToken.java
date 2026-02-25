package com.bapsdelhibalmandal.balbalika_management_system.Security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class PhoneAuthenticationToken extends AbstractAuthenticationToken {

    private final String phoneNumber;
    private String otp;

    public PhoneAuthenticationToken(String phoneNumber, String otp) {
        super(null);
        this.phoneNumber = phoneNumber;
        this.otp = otp;
        setAuthenticated(false);
    }

    public PhoneAuthenticationToken(String phoneNumber, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.phoneNumber = phoneNumber;
        this.otp = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return otp;
    }

    @Override
    public Object getPrincipal() {
        return phoneNumber;
    }
}
