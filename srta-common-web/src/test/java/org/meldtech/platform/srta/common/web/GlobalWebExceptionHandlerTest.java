package org.meldtech.platform.srta.common.web;

import org.junit.jupiter.api.Test;
import org.meldtech.platform.srta.common.api.ApiResponse;
import org.meldtech.platform.srta.common.api.ErrorCode;
import org.meldtech.platform.srta.common.api.ValidationError;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalWebExceptionHandlerTest {

    private final GlobalWebExceptionHandler handler = new GlobalWebExceptionHandler();

    @Test
    void testHandleResponseStatusException_NotFound() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_Unauthorized() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_AUTHENTICATION_FAILED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedAccountLocked() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ACCOUNT_LOCKED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_ACCOUNT_LOCKED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedAccountInactive() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ACCOUNT_INACTIVE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_ACCOUNT_INACTIVE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedInvalidCredentials() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_INVALID_CREDENTIALS.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedSessionExpired() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "SESSION_EXPIRED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_SESSION_EXPIRED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedInvalidCurrentPassword() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CURRENT_PASSWORD");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_INVALID_CURRENT_PASSWORD.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedOtpSessionExpired() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP_SESSION_EXPIRED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_OTP_SESSION_EXPIRED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedInvalidRefreshToken() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_INVALID_REFRESH_TOKEN.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnauthorizedRefreshTokenExpired() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_Forbidden() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_FORBIDDEN.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ForbiddenPasswordChangeRequired() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "PASSWORD_CHANGE_REQUIRED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_PASSWORD_CHANGE_REQUIRED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ForbiddenPasswordManagedByAd() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "PASSWORD_MANAGED_BY_AD");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_PASSWORD_MANAGED_BY_AD.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ForbiddenSelfAuthorizationBlocked() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "SELF_AUTHORIZATION_BLOCKED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_SELF_AUTHORIZATION_BLOCKED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ForbiddenSelfApprovalBlocked() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "SELF_APPROVAL_BLOCKED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_SELF_APPROVAL_BLOCKED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableSamePassword() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "SAME_PASSWORD");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.AUTH_SAME_PASSWORD.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessablePasswordMismatch() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_MISMATCH");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.AUTH_PASSWORD_MISMATCH.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableWeakPassword() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "WEAK_PASSWORD");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.AUTH_WEAK_PASSWORD.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessablePasswordReuse() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PASSWORD_REUSE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.AUTH_PASSWORD_REUSE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableRoleNotFound() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "ROLE_NOT_FOUND");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.ROLE_NOT_FOUND.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableInvalidConditionField() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CONDITION_FIELD");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_CONDITION_FIELD.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableInvalidOperator() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_OPERATOR");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_OPERATOR.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableInvalidTargetAction() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_TARGET_ACTION");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_TARGET_ACTION.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableGlCodeNotFound() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "GL_CODE_NOT_FOUND");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.GL_CODE_NOT_FOUND.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableDeletionNotPermitted() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "DELETION_NOT_PERMITTED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.DELETION_NOT_PERMITTED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableCbsAccountInactive() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "CBS_ACCOUNT_INACTIVE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.CBS_ACCOUNT_INACTIVE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableInsufficientBalance() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableRejectionReasonRequired() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "REJECTION_REASON_REQUIRED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.REJECTION_REASON_REQUIRED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableInvalidActionForStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_ACTION_FOR_STATUS");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.INVALID_ACTION_FOR_STATUS.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableFailedManualQuoteUpdate() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "FAILED_MANUAL_QUOTE_UPDATE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.FAILED_MANUAL_QUOTE_UPDATE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_UnprocessableQuoteAlreadyDeleted() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "QUOTE_ALREADY_DELETED");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals(ErrorCode.QUOTE_ALREADY_DELETED.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ServiceUnavailableAuthServiceUnavailable() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_AUTH_SERVICE_UNAVAILABLE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ServiceUnavailableCbsUnavailable() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "CBS_UNAVAILABLE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(ErrorCode.CBS_UNAVAILABLE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ConflictConcurrentModification() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.CONCURRENT_MODIFICATION.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ConflictRoleInUse() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "ROLE_IN_USE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.ROLE_IN_USE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ConflictCurrencyInUse() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "CURRENCY_IN_USE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.CURRENCY_IN_USE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ConflictWorkflowInUse() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "WORKFLOW_IN_USE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.WORKFLOW_IN_USE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_ConflictGlAccountAlreadyExists() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "GL_ACCOUNT_ALREADY_EXISTS");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(ErrorCode.GL_ACCOUNT_ALREADY_EXISTS.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_BadRequestInvalidPage() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_PAGE");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.INVALID_PAGE.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleResponseStatusException_BadRequestInvalidCsvFormat() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_CSV_FORMAT");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorCode.INVALID_CSV_FORMAT.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getBody().getCode());
    }

    @Test
    void testHandleValidationException_returnsFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "email", "bad@", false,
                null, null, "must be a valid email"));
        bindingResult.addError(new FieldError("target", "name", null, false,
                null, null, "must not be blank"));

        Method method = Object.class.getMethod("toString");
        MethodParameter methodParameter = new MethodParameter(method, -1);
        WebExchangeBindException ex = new WebExchangeBindException(methodParameter, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.VALIDATION_ERROR.getCode(), response.getBody().getCode());
        assertFalse(response.getBody().isSuccess());

        List<ValidationError> errors = response.getBody().getErrors();
        assertEquals(2, errors.size());
        assertEquals("email", errors.get(0).getField());
        assertEquals("must be a valid email", errors.get(0).getMessage());
        assertEquals("name", errors.get(1).getField());
    }

    @Test
    void testHandleResponseStatusException_detailPreserved() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade 123 not found");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals("Trade 123 not found", response.getBody().getDetail());
    }
}
