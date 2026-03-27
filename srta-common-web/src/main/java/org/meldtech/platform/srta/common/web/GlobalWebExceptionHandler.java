package org.meldtech.platform.srta.common.web;

import lombok.extern.slf4j.Slf4j;
import org.meldtech.platform.srta.common.api.ApiResponse;
import org.meldtech.platform.srta.common.api.ErrorCode;
import org.meldtech.platform.srta.common.api.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for Reactive web applications.
 * Maps exceptions to a standard {@link ApiResponse} envelope.
 */
@Slf4j
@RestControllerAdvice
public class GlobalWebExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("Response status exception: {}", ex.getReason());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ErrorCode errorCode = mapStatusToErrorCode(status);
        ApiResponse<Void> response = ApiResponse.error(errorCode, ex.getReason());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<ValidationError> errors = ex.getFieldErrors().stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .code(error.getCode())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.validationError(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ErrorCode mapStatusToErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.INTERNAL_ERROR; // Default if specific isn't found not available
            case UNAUTHORIZED -> ErrorCode.AUTHENTICATION_FAILED;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case BAD_REQUEST -> ErrorCode.VALIDATION_ERROR;
            case CONFLICT -> ErrorCode.USERNAME_EXISTS; // Or generic email/username exists
            case UNPROCESSABLE_ENTITY -> ErrorCode.WORKFLOW_VIOLATION;
            case TOO_MANY_REQUESTS -> ErrorCode.RATE_LIMIT_EXCEEDED;
            case SERVICE_UNAVAILABLE -> ErrorCode.CBS_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
