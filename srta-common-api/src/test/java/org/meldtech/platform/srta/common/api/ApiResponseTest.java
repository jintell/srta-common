package org.meldtech.platform.srta.common.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_wrapsDataCorrectly() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertTrue(response.isSuccess());
        assertEquals("hello", response.getData());
        assertEquals(ErrorCode.SUCCESS.getCode(), response.getCode());
        assertNotNull(response.getTimestamp());
        assertInstanceOf(Instant.class, response.getTimestamp());
    }

    @Test
    void error_setsCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.INTERNAL_ERROR);

        assertFalse(response.isSuccess());
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), response.getCode());
        assertEquals(ErrorCode.INTERNAL_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void withDetail_preservesOriginalAndAddsDetail() {
        ApiResponse<Void> original = ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND);
        ApiResponse<Void> detailed = original.withDetail("Trade 42 not found");

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), detailed.getCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getMessage(), detailed.getMessage());
        assertEquals("Trade 42 not found", detailed.getDetail());
        assertNull(original.getDetail());
    }

    @Test
    void validationErrors_includedInResponse() {
        List<ValidationError> errors = List.of(
                ValidationError.builder().field("email").message("invalid").build()
        );
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .errors(errors)
                .build();

        assertEquals(1, response.getErrors().size());
        assertEquals("email", response.getErrors().get(0).getField());
    }

    @Test
    void paged_wrapsPagedResponse() {
        PagedResponse<String> page = PagedResponse.of(List.of("a", "b"), 0, 10, 2);
        ApiResponse<PagedResponse<String>> response = ApiResponse.paged(page);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().getContent().size());
    }
}
