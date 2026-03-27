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
    void testHandleResponseStatusException_Forbidden() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ErrorCode.AUTH_FORBIDDEN.getCode(), response.getBody().getCode());
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
