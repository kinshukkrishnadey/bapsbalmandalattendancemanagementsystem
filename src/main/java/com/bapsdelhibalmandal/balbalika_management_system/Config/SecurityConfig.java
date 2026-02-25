package com.bapsdelhibalmandal.balbalika_management_system.Config;

import com.bapsdelhibalmandal.balbalika_management_system.Security.ApiAuthenticationFilter;
import com.bapsdelhibalmandal.balbalika_management_system.Security.CustomPermissionEvaluator;
import com.bapsdelhibalmandal.balbalika_management_system.Security.JwtFilter;
import com.bapsdelhibalmandal.balbalika_management_system.Security.PhoneNumberAuthenticationProvider;
import com.bapsdelhibalmandal.balbalika_management_system.repository.UserRepository;
import com.bapsdelhibalmandal.balbalika_management_system.util.JwtUtil;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private PhoneNumberAuthenticationProvider phoneAuthProvider;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomPermissionEvaluator permissionEvaluator;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/h2-console/**", "/").permitAll()
                        .anyRequest().authenticated()

                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())  // ✅ for H2 console
                )
                .authenticationProvider(phoneAuthProvider)
                .addFilterBefore((Filter) jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore((Filter) apiAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, userRepository);
    }

    @Bean
    public ApiAuthenticationFilter apiAuthenticationFilter() {
        AuthenticationManager authManager = new ProviderManager(List.of(phoneAuthProvider));
        return new ApiAuthenticationFilter("/auth/login", authManager, jwtUtil);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(phoneAuthProvider));
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
