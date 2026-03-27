package org.meldtech.platform.srta.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Standard envelope for all API responses.
 *
 * @param <T> The type of the data payload.
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    Instant timestamp = Instant.now();
    
    boolean success;
    
    String code;
    
    String message;

    String detail;

    T data;

    List<ValidationError> errors;

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    /**
     * Create an error response with an error code.
     */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * Return a copy of this response with an additional detail message.
     */
    public ApiResponse<T> withDetail(String detail) {
        return ApiResponse.<T>builder()
                .timestamp(this.timestamp)
                .success(this.success)
                .code(this.code)
                .message(this.message)
                .detail(detail)
                .data(this.data)
                .errors(this.errors)
                .build();
    }

    /**
     * Create a successful paged response wrapped in the standard envelope.
     */
    public static <T> ApiResponse<PagedResponse<T>> paged(PagedResponse<T> page) {
        return ApiResponse.<PagedResponse<T>>builder()
                .success(true)
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .data(page)
                .build();
    }

    /**
     * Create a validation error response.
     */
    public static <T> ApiResponse<T> validationError(List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .errors(errors)
                .build();
    }
}
