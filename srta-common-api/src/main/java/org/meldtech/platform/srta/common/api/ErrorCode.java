package org.meldtech.platform.srta.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Centralized list of application error codes.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Standard Errors
    SUCCESS("SUCCESS", "Operation successful"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Invalid credentials"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "JWT access token expired"),
    TOKEN_BLACKLISTED("TOKEN_BLACKLISTED", "JWT revoked (logout/rotation)"),
    JWT_MISSING_JTI("JWT_MISSING_JTI", "JWT has no jti claim"),
    FORBIDDEN("FORBIDDEN", "Insufficient role/permission"),
    SELF_APPROVAL_FORBIDDEN("SELF_APPROVAL_FORBIDDEN", "Inputter cannot approve own trade"),
    TRADE_NOT_FOUND("TRADE_NOT_FOUND", "Trade ID does not exist"),
    USER_NOT_FOUND("USER_NOT_FOUND", "User ID does not exist"),
    RATE_NOT_FOUND("RATE_NOT_FOUND", "No active rate for pair/branch"),
    DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND", "Deal slip not yet generated"),
    VALIDATION_ERROR("VALIDATION_ERROR", "JSR-380 constraint violation"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "pageSize out of 1-100 range"),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "CBS: account does not exist"),
    ACCOUNT_FROZEN("ACCOUNT_FROZEN", "CBS: account frozen"),
    ACCOUNT_DORMANT("ACCOUNT_DORMANT", "CBS: account dormant"),
    WORKFLOW_VIOLATION("WORKFLOW_VIOLATION", "Invalid state transition"),
    RATE_OUTSIDE_TOLERANCE("RATE_OUTSIDE_TOLERANCE", "Entered rate deviates from TMS"),
    SPREAD_NOT_CONFIGURED("SPREAD_NOT_CONFIGURED", "No spread config for pair"),
    CURRENCY_PAIR_INACTIVE("CURRENCY_PAIR_INACTIVE", "Currency pair not active"),
    RATE_BACKDATE_EXCEEDED("RATE_BACKDATE_EXCEEDED", "effectiveFrom too old"),
    OTP_EXPIRED("OTP_EXPIRED", "OTP TTL elapsed"),
    OTP_INVALID("OTP_INVALID", "OTP code incorrect"),
    OTP_MAX_ATTEMPTS("OTP_MAX_ATTEMPTS", "OTP attempts exhausted"),
    USERNAME_EXISTS("USERNAME_EXISTS", "Duplicate username"),
    EMAIL_EXISTS("EMAIL_EXISTS", "Duplicate email"),
    TRADE_AMOUNT_EXCEEDS_LIMIT("TRADE_AMOUNT_EXCEEDS_LIMIT", "Above single-trade limit"),
    TRADE_DAILY_LIMIT_EXCEEDED("TRADE_DAILY_LIMIT_EXCEEDED", "Customer daily limit hit"),
    BULK_UPLOAD_EMPTY("BULK_UPLOAD_EMPTY", "No valid rows in upload file"),
    EXPORT_LIMIT_EXCEEDED("EXPORT_LIMIT_EXCEEDED", "Too many rows for export"),
    REPORT_DATE_RANGE_REQUIRED("REPORT_DATE_RANGE_REQUIRED", "Date range mandatory"),
    CBS_UNAVAILABLE("CBS_UNAVAILABLE", "CBS circuit breaker open"),
    TMS_UNAVAILABLE("TMS_UNAVAILABLE", "TMS circuit breaker open"),
    SMTP_SEND_FAILURE("SMTP_SEND_FAILURE", "Email send failed"),
    REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED", "Jasper/POI exception"),
    EXPORT_GENERATION_FAILED("EXPORT_GENERATION_FAILED", "Excel generation failed"),
    EMAIL_RATE_LIMIT_EXCEEDED("EMAIL_RATE_LIMIT_EXCEEDED", "Too many emails to recipient"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "API gateway rate limit hit"),
    BULKHEAD_FULL("BULKHEAD_FULL", "Too many concurrent CBS calls"),
    ACCOUNTING_IMBALANCE("ACCOUNTING_IMBALANCE", "GL entries do not balance"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Unhandled exception");

    private final String code;
    private final String message;
}
