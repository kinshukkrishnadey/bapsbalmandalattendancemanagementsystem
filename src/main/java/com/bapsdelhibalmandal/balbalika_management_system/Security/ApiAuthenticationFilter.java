package com.bapsdelhibalmandal.balbalika_management_system.Security;



import com.bapsdelhibalmandal.balbalika_management_system.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;
import java.util.Map;

public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();


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

        return getAuthenticationManager().authenticate(new PhoneAuthenticationToken(phone, otp));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {
        String token = jwtUtil.generateToken(authResult);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "message", "Login successful",
                "token", token,
                "type", "Bearer"
        )));
    }


}
