package com.bapsdelhibalmandal.balbalika_management_system.Config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Debug filter to log Content-Type at the earliest possible point in the filter chain.
 * This helps identify where Content-Type is being changed.
 */
@Component
@Order(1)
public class ContentTypeDebugFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypeDebugFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest) {
            String path = httpRequest.getRequestURI();
            String method = httpRequest.getMethod();
            
            // Only log for kid-related endpoints
            if (path != null && path.startsWith("/api/kid/")) {
                String logMsg = String.format("=== CONTENT-TYPE DEBUG FILTER ===\nMethod: %s\nPath: %s\nContent-Type: %s\nContent-Length: %s",
                        method, path, httpRequest.getContentType(), httpRequest.getContentLength());
                System.out.println(logMsg);
                logger.info(logMsg);
                
                System.out.println("All Headers:");
                httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                    String headerLine = "  " + headerName + ": " + httpRequest.getHeader(headerName);
                    System.out.println(headerLine);
                    logger.info(headerLine);
                });
                System.out.println("==================================");
                logger.info("==================================");
            }
        }
        
        chain.doFilter(request, response);
    }
}
