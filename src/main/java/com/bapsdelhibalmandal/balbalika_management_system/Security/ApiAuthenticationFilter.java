package com.bapsdelhibalmandal.balbalika_management_system.Security;



import com.bapsdelhibalmandal.balbalika_management_system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtUtil jwtUtil;


    public ApiAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        String phone = request.getParameter("phone");
        String otp = request.getParameter("otp");

        if (phone == null || otp == null) {
            throw new IllegalArgumentException("Phone and OTP must be provided");
        }

        Authentication authRequest = new UsernamePasswordAuthenticationToken(phone, otp);
        return getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {
        // Token generation or redirect logic goes here
        response.getWriter().write("Login successful");
    }


}
