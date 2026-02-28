package com.bapsdelhibalmandal.balbalika_management_system.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = "Invalid path parameter '" + param + "': '" + value + "' is not a valid ID. " +
                "Ensure the frontend sends a numeric ID (e.g. kidId, zoneId) and does not call the API before the ID is available.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        System.out.println("=== HttpMediaTypeNotSupportedException DEBUG ===");
        System.out.println("Content-Type received: " + ex.getContentType());
        System.out.println("Supported media types: " + ex.getSupportedMediaTypes());
        System.out.println("Message: " + ex.getMessage());
        System.out.println("================================================");
        
        String message = "Content-Type '" + ex.getContentType() + "' is not supported. " +
                "For kid register/update, use multipart/form-data with parts: 'kid' (JSON) and 'photo' (file). " +
                "Do not set Content-Type manually when using FormData - let the client set it with the boundary.";
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, String>> handleMissingPart(MissingServletRequestPartException ex) {
        String message = "Missing required part: '" + ex.getRequestPartName() + "'. " +
                "Use multipart/form-data with parts: 'kid' (JSON) and 'photo' (file, optional).";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }
}
