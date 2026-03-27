package org.meldtech.platform.srta.common.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meldtech.platform.srta.common.api.ApiResponse;
import org.meldtech.platform.srta.common.api.ErrorCode;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter-level exception handler for WebFlux.
 * Catches exceptions that {@code @RestControllerAdvice} cannot intercept
 * (e.g., errors thrown in {@link org.springframework.web.server.WebFilter} chains
 * or {@code RouterFunction} routes).
 *
 * <p>Runs at order {@code -2} to execute before Spring's default error handler.
 */
@Slf4j
@Order(-2)
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        ErrorCode errorCode;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorCode = mapStatusToErrorCode(status);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = ErrorCode.INTERNAL_ERROR;
            log.error("Unhandled filter-level exception", ex);
        }

        ApiResponse<Void> response = ApiResponse.error(errorCode);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(response);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize error response", e);
            return Mono.error(e);
        }
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
