package com.bapsdelhibalmandal.balbalika_management_system.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * Configuration to bypass content negotiation for multipart/form-data requests.
 * 
 * The issue is that Spring's content negotiation happens BEFORE multipart parsing,
 * and SpringDoc's defaults only support JSON/YAML. This causes multipart requests
 * to be rejected before they can be parsed.
 */
@Configuration
public class MultipartContentNegotiationConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // Configure defaults - don't replace strategies, just configure them
        configurer.favorParameter(false)
                  .favorPathExtension(false)
                  .ignoreAcceptHeader(true)  // Ignore Accept header to prevent issues
                  .defaultContentType(MediaType.APPLICATION_JSON);
        
        // Don't replace strategies - Spring will use defaults which should work
        // The key is that we're ignoring Accept header and using Content-Type directly
        // This should allow multipart/form-data to pass through when @RequestPart is used
    }
}
