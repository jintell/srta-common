package org.meldtech.platform.srta.common.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Centralized list of application error codes.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── General ─────────────────────────────────────────────────────────
    SUCCESS("SUCCESS", "Operation successful"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Unhandled exception"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource does not exist"),
    CONFLICT("CONFLICT", "Resource conflict or duplicate"),
    DOWNSTREAM_UNAVAILABLE("DOWNSTREAM_UNAVAILABLE", "Downstream service unavailable"),
    VALIDATION_ERROR("VALIDATION_ERROR", "JSR-380 constraint violation"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "pageSize out of 1-100 range"),

    // ── Auth ────────────────────────────────────────────────────────────
    AUTH_AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Invalid credentials"),
    AUTH_TOKEN_EXPIRED("TOKEN_EXPIRED", "JWT access token expired"),
    AUTH_TOKEN_BLACKLISTED("TOKEN_BLACKLISTED", "JWT revoked (logout/rotation)"),
    AUTH_JWT_MISSING_JTI("JWT_MISSING_JTI", "JWT has no jti claim"),
    AUTH_FORBIDDEN("FORBIDDEN", "Insufficient role/permission"),
    AUTH_SELF_APPROVAL_FORBIDDEN("SELF_APPROVAL_FORBIDDEN", "Inputter cannot approve own trade"),
    AUTH_OTP_EXPIRED("OTP_EXPIRED", "OTP TTL elapsed"),
    AUTH_OTP_INVALID("OTP_INVALID", "OTP code incorrect"),
    AUTH_OTP_MAX_ATTEMPTS("OTP_MAX_ATTEMPTS", "OTP attempts exhausted"),
    AUTH_USERNAME_EXISTS("USERNAME_EXISTS", "Duplicate username"),
    AUTH_EMAIL_EXISTS("EMAIL_EXISTS", "Duplicate email"),

    // ── Trade ───────────────────────────────────────────────────────────
    TRADE_NOT_FOUND("TRADE_NOT_FOUND", "Trade ID does not exist"),
    TRADE_WORKFLOW_VIOLATION("WORKFLOW_VIOLATION", "Invalid state transition"),
    TRADE_AMOUNT_EXCEEDS_LIMIT("TRADE_AMOUNT_EXCEEDS_LIMIT", "Above single-trade limit"),
    TRADE_DAILY_LIMIT_EXCEEDED("TRADE_DAILY_LIMIT_EXCEEDED", "Customer daily limit hit"),
    TRADE_RATE_OUTSIDE_TOLERANCE("RATE_OUTSIDE_TOLERANCE", "Entered rate deviates from TMS"),
    TRADE_SPREAD_NOT_CONFIGURED("SPREAD_NOT_CONFIGURED", "No spread config for pair"),
    TRADE_CURRENCY_PAIR_INACTIVE("CURRENCY_PAIR_INACTIVE", "Currency pair not active"),
    TRADE_RATE_BACKDATE_EXCEEDED("RATE_BACKDATE_EXCEEDED", "effectiveFrom too old"),
    TRADE_ACCOUNTING_IMBALANCE("ACCOUNTING_IMBALANCE", "GL entries do not balance"),

    // ── CBS (Core Banking) ──────────────────────────────────────────────
    CBS_ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "CBS: account does not exist"),
    CBS_ACCOUNT_FROZEN("ACCOUNT_FROZEN", "CBS: account frozen"),
    CBS_ACCOUNT_DORMANT("ACCOUNT_DORMANT", "CBS: account dormant"),
    CBS_UNAVAILABLE("CBS_UNAVAILABLE", "CBS circuit breaker open"),
    CBS_BULKHEAD_FULL("BULKHEAD_FULL", "Too many concurrent CBS calls"),

    // ── TMS (Treasury Management) ───────────────────────────────────────
    TMS_UNAVAILABLE("TMS_UNAVAILABLE", "TMS circuit breaker open"),

    // ── User ────────────────────────────────────────────────────────────
    USER_NOT_FOUND("USER_NOT_FOUND", "User ID does not exist"),
    USER_RATE_NOT_FOUND("RATE_NOT_FOUND", "No active rate for pair/branch"),

    // ── Document / Report / Export ──────────────────────────────────────
    DOC_DOCUMENT_NOT_FOUND("DOCUMENT_NOT_FOUND", "Deal slip not yet generated"),
    DOC_REPORT_DATE_RANGE_REQUIRED("REPORT_DATE_RANGE_REQUIRED", "Date range mandatory"),
    DOC_REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED", "Jasper/POI exception"),
    DOC_EXPORT_GENERATION_FAILED("EXPORT_GENERATION_FAILED", "Excel generation failed"),
    DOC_EXPORT_LIMIT_EXCEEDED("EXPORT_LIMIT_EXCEEDED", "Too many rows for export"),
    DOC_BULK_UPLOAD_EMPTY("BULK_UPLOAD_EMPTY", "No valid rows in upload file"),

    // ── Notification ────────────────────────────────────────────────────
    NOTIFY_SMTP_SEND_FAILURE("SMTP_SEND_FAILURE", "Email send failed"),
    NOTIFY_EMAIL_RATE_LIMIT_EXCEEDED("EMAIL_RATE_LIMIT_EXCEEDED", "Too many emails to recipient"),

    // ── Rate Limiting ───────────────────────────────────────────────────
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "API gateway rate limit hit");

    private final String code;
    private final String message;
}
