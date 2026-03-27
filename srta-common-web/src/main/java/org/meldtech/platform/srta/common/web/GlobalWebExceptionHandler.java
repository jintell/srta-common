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
        ApiResponse<Void> response = ApiResponse.<Void>error(errorCode).withDetail(ex.getReason());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(WebExchangeBindException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<ValidationError> errors = ex.getFieldErrors().stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField() != null ? error.getField() : "_global")
                        .code(error.getCode())
                        .message(error.getDefaultMessage())
                        .rejectedValue(String.valueOf(error.getRejectedValue()))
                        .build())
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.validationError(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ErrorCode mapStatusToErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND;
            case UNAUTHORIZED -> ErrorCode.AUTH_AUTHENTICATION_FAILED;
            case FORBIDDEN -> ErrorCode.AUTH_FORBIDDEN;
            case BAD_REQUEST -> ErrorCode.VALIDATION_ERROR;
            case CONFLICT -> ErrorCode.CONFLICT;
            case UNPROCESSABLE_ENTITY -> ErrorCode.TRADE_WORKFLOW_VIOLATION;
            case TOO_MANY_REQUESTS -> ErrorCode.RATE_LIMIT_EXCEEDED;
            case SERVICE_UNAVAILABLE -> ErrorCode.DOWNSTREAM_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
