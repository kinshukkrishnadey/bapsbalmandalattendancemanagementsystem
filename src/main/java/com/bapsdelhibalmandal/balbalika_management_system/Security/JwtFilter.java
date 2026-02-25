package com.bapsdelhibalmandal.balbalika_management_system.Security;

import com.bapsdelhibalmandal.balbalika_management_system.model.User;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import com.bapsdelhibalmandal.balbalika_management_system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwtUtil.validateToken(jwt)) {
                String phone = jwtUtil.extractPhone(jwt);
                User user = userRepository.findByPhoneNumber(phone).orElse(null);
                if (user != null) {
                    List<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName()))
                            .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(phone, null, authorities);
                    auth.setDetails(user);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request,response);

    }
}
