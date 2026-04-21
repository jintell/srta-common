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
    INVALID_CSV_FORMAT("INVALID_CSV_FORMAT", "CSV format is invalid"),
    INVALID_PAGE("INVALID_PAGE", "page must be greater than 0"),
    INVALID_PAGE_SIZE("INVALID_PAGE_SIZE", "pageSize out of 1-100 range"),
    CONCURRENT_MODIFICATION("CONCURRENT_MODIFICATION", "Version mismatch on optimistic lock"),

    // ── Auth ────────────────────────────────────────────────────────────
    AUTH_AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Invalid credentials"),
    AUTH_INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid credentials"),
    AUTH_ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is locked"),
    AUTH_ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", "Account is inactive"),
    AUTH_AUTH_SERVICE_UNAVAILABLE("AUTH_SERVICE_UNAVAILABLE", "Authentication service unavailable"),
    AUTH_TOKEN_EXPIRED("TOKEN_EXPIRED", "JWT access token expired"),
    AUTH_TOKEN_BLACKLISTED("TOKEN_BLACKLISTED", "JWT revoked (logout/rotation)"),
    AUTH_JWT_MISSING_JTI("JWT_MISSING_JTI", "JWT has no jti claim"),
    AUTH_FORBIDDEN("FORBIDDEN", "Insufficient role/permission"),
    AUTH_SELF_APPROVAL_FORBIDDEN("SELF_APPROVAL_FORBIDDEN", "Inputter cannot approve own trade"),
    AUTH_SELF_AUTHORIZATION_BLOCKED("SELF_AUTHORIZATION_BLOCKED", "Self-authorization is blocked"),
    AUTH_OTP_EXPIRED("OTP_EXPIRED", "OTP TTL elapsed"),
    AUTH_OTP_INVALID("OTP_INVALID", "OTP code incorrect"),
    AUTH_OTP_MAX_ATTEMPTS("OTP_MAX_ATTEMPTS", "OTP attempts exhausted"),
    AUTH_OTP_SESSION_EXPIRED("OTP_SESSION_EXPIRED", "OTP session expired"),
    AUTH_SESSION_EXPIRED("SESSION_EXPIRED", "Session expired"),
    AUTH_PASSWORD_CHANGE_REQUIRED("PASSWORD_CHANGE_REQUIRED", "Password change required"),
    AUTH_INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "Current password is invalid"),
    AUTH_SAME_PASSWORD("SAME_PASSWORD", "New password must be different from current password"),
    AUTH_PASSWORD_MISMATCH("PASSWORD_MISMATCH", "New password and confirm password do not match"),
    AUTH_WEAK_PASSWORD("WEAK_PASSWORD", "Password does not meet complexity requirements"),
    AUTH_PASSWORD_REUSE("PASSWORD_REUSE", "Password was used recently"),
    AUTH_PASSWORD_MANAGED_BY_AD("PASSWORD_MANAGED_BY_AD", "Password is managed by Active Directory"),
    AUTH_INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Refresh token is invalid"),
    AUTH_REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "Refresh token has expired"),
    AUTH_USERNAME_EXISTS("USERNAME_EXISTS", "Duplicate username"),
    AUTH_EMAIL_EXISTS("EMAIL_EXISTS", "Duplicate email"),
    AUTH_SELF_APPROVAL_BLOCKED("SELF_APPROVAL_BLOCKED", "Approver must differ from submitter"),

    // ── IAM / Roles ─────────────────────────────────────────────────────
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role ID does not exist"),
    ROLE_IN_USE("ROLE_IN_USE", "Role is assigned to one or more users"),

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
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Available balance is insufficient"),
    REJECTION_REASON_REQUIRED("REJECTION_REASON_REQUIRED", "Rejection reason is required"),
    INVALID_ACTION_FOR_STATUS("INVALID_ACTION_FOR_STATUS", "Action is invalid for current status"),
    FAILED_MANUAL_QUOTE_UPDATE("FAILED_MANUAL_QUOTE_UPDATE", "Manual quote update failed"),
    QUOTE_ALREADY_DELETED("QUOTE_ALREADY_DELETED", "Quote has already been deleted"),
    WORKFLOW_IN_USE("WORKFLOW_IN_USE", "Workflow has in-flight trades"),
    INVALID_CONDITION_FIELD("INVALID_CONDITION_FIELD", "Unsupported workflow condition field"),
    INVALID_OPERATOR("INVALID_OPERATOR", "Operator is invalid for condition field"),
    INVALID_TARGET_ACTION("INVALID_TARGET_ACTION", "Target action is not a workflow transition"),
    GL_ACCOUNT_ALREADY_EXISTS("GL_ACCOUNT_ALREADY_EXISTS", "GL account for combination already exists"),
    GL_CODE_NOT_FOUND("GL_CODE_NOT_FOUND", "No active GL code found for settlement"),
    CURRENCY_IN_USE("CURRENCY_IN_USE", "Currency is referenced by other entities"),
    DELETION_NOT_PERMITTED("DELETION_NOT_PERMITTED", "Deletion is not permitted"),

    // ── CBS (Core Banking) ──────────────────────────────────────────────
    CBS_ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "CBS: account does not exist"),
    CBS_ACCOUNT_FROZEN("ACCOUNT_FROZEN", "CBS: account frozen"),
    CBS_ACCOUNT_DORMANT("ACCOUNT_DORMANT", "CBS: account dormant"),
    CBS_ACCOUNT_INACTIVE("CBS_ACCOUNT_INACTIVE", "CBS: account is inactive"),
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
