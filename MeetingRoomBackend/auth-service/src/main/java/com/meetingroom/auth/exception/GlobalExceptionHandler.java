package com.meetingroom.auth.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : MDC.get("X-B3-TraceId");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Authentication business exception [traceId={}]: {}", getTraceId(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatus().value())
                .message(ex.getMessage())
                .traceId(getTraceId())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now().toString())
                .build();
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, HttpServletRequest request) {
        log.error("Downstream User Service call failed [traceId={}]: status={}, message={}", getTraceId(), ex.status(), ex.getMessage());
        HttpStatus status = ex.status() > 0 ? HttpStatus.resolve(ex.status()) : HttpStatus.INTERNAL_SERVER_ERROR;
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorMessage = ex.contentUTF8().isEmpty() ? ex.getMessage() : ex.contentUTF8();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .message("Downstream user-service error: " + errorMessage)
                .traceId(getTraceId())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now().toString())
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation exception for auth request [traceId={}]: {}", getTraceId(), request.getRequestURI());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed for request fields: " + errors.toString())
                .validationErrors(errors)
                .traceId(getTraceId())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now().toString())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception in auth-service [traceId={}]: {}", getTraceId(), request.getRequestURI(), ex);
        String detailedMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Authentication Error: " + detailedMessage)
                .traceId(getTraceId())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now().toString())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
