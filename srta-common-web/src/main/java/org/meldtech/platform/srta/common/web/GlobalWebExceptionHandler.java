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
        ErrorCode errorCode = mapStatusToErrorCode(status, ex.getReason());
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

    private ErrorCode mapStatusToErrorCode(HttpStatus status, String reason) {
        ErrorCode reasonCode = resolveByReason(reason);
        return switch (status) {
            case NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND;
            case UNAUTHORIZED -> isUnauthorizedReasonCode(reasonCode)
                    ? reasonCode
                    : ErrorCode.AUTH_AUTHENTICATION_FAILED;
            case FORBIDDEN -> isForbiddenReasonCode(reasonCode)
                    ? reasonCode
                    : ErrorCode.AUTH_FORBIDDEN;
            case BAD_REQUEST -> ErrorCode.INVALID_PAGE.equals(reasonCode)
                    ? ErrorCode.INVALID_PAGE
                    : ErrorCode.INVALID_CSV_FORMAT.equals(reasonCode)
                    ? ErrorCode.INVALID_CSV_FORMAT
                    : ErrorCode.VALIDATION_ERROR;
            case CONFLICT -> ErrorCode.CONCURRENT_MODIFICATION.equals(reasonCode)
                    || ErrorCode.ROLE_IN_USE.equals(reasonCode)
                    || ErrorCode.CURRENCY_IN_USE.equals(reasonCode)
                    || ErrorCode.WORKFLOW_IN_USE.equals(reasonCode)
                    || ErrorCode.GL_ACCOUNT_ALREADY_EXISTS.equals(reasonCode)
                    ? reasonCode
                    : ErrorCode.CONFLICT;
            case UNPROCESSABLE_ENTITY, UNPROCESSABLE_CONTENT -> isUnprocessableReasonCode(reasonCode)
                    ? reasonCode
                    : ErrorCode.TRADE_WORKFLOW_VIOLATION;
            case TOO_MANY_REQUESTS -> ErrorCode.RATE_LIMIT_EXCEEDED;
            case SERVICE_UNAVAILABLE -> ErrorCode.AUTH_AUTH_SERVICE_UNAVAILABLE.equals(reasonCode)
                    || ErrorCode.CBS_UNAVAILABLE.equals(reasonCode)
                    ? reasonCode
                    : ErrorCode.DOWNSTREAM_UNAVAILABLE;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

    private boolean isUnauthorizedReasonCode(ErrorCode reasonCode) {
        return ErrorCode.AUTH_ACCOUNT_LOCKED.equals(reasonCode)
                || ErrorCode.AUTH_ACCOUNT_INACTIVE.equals(reasonCode)
                || ErrorCode.AUTH_INVALID_CREDENTIALS.equals(reasonCode)
                || ErrorCode.AUTH_SESSION_EXPIRED.equals(reasonCode)
                || ErrorCode.AUTH_INVALID_CURRENT_PASSWORD.equals(reasonCode)
                || ErrorCode.AUTH_OTP_SESSION_EXPIRED.equals(reasonCode)
                || ErrorCode.AUTH_INVALID_REFRESH_TOKEN.equals(reasonCode)
                || ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED.equals(reasonCode);
    }

    private boolean isForbiddenReasonCode(ErrorCode reasonCode) {
        return ErrorCode.AUTH_PASSWORD_CHANGE_REQUIRED.equals(reasonCode)
                || ErrorCode.AUTH_PASSWORD_MANAGED_BY_AD.equals(reasonCode)
                || ErrorCode.AUTH_SELF_AUTHORIZATION_BLOCKED.equals(reasonCode)
                || ErrorCode.AUTH_SELF_APPROVAL_BLOCKED.equals(reasonCode);
    }

    private boolean isUnprocessableReasonCode(ErrorCode reasonCode) {
        return ErrorCode.AUTH_SAME_PASSWORD.equals(reasonCode)
                || ErrorCode.AUTH_PASSWORD_MISMATCH.equals(reasonCode)
                || ErrorCode.AUTH_WEAK_PASSWORD.equals(reasonCode)
                || ErrorCode.AUTH_PASSWORD_REUSE.equals(reasonCode)
                || ErrorCode.ROLE_NOT_FOUND.equals(reasonCode)
                || ErrorCode.INVALID_CONDITION_FIELD.equals(reasonCode)
                || ErrorCode.INVALID_OPERATOR.equals(reasonCode)
                || ErrorCode.INVALID_TARGET_ACTION.equals(reasonCode)
                || ErrorCode.GL_CODE_NOT_FOUND.equals(reasonCode)
                || ErrorCode.DELETION_NOT_PERMITTED.equals(reasonCode)
                || ErrorCode.CBS_ACCOUNT_INACTIVE.equals(reasonCode)
                || ErrorCode.INSUFFICIENT_BALANCE.equals(reasonCode)
                || ErrorCode.REJECTION_REASON_REQUIRED.equals(reasonCode)
                || ErrorCode.INVALID_ACTION_FOR_STATUS.equals(reasonCode)
                || ErrorCode.FAILED_MANUAL_QUOTE_UPDATE.equals(reasonCode)
                || ErrorCode.QUOTE_ALREADY_DELETED.equals(reasonCode);
    }

    private ErrorCode resolveByReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return ErrorCode.INTERNAL_ERROR;
        }
        for (ErrorCode code : ErrorCode.values()) {
            if (code.getCode().equals(reason)) {
                return code;
            }
        }
        return ErrorCode.INTERNAL_ERROR;
    }
}
