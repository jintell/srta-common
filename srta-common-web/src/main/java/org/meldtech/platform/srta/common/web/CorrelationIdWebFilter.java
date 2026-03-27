package org.meldtech.platform.srta.common.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Filter to generate and propagate X-Correlation-ID for tracing.
 * Uses Reactor Context for MDC-like propagation in reactive streams.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_KEY = "correlationId";
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("^[a-zA-Z0-9\\-]{8,64}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String raw = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = (raw != null && SAFE_CORRELATION_ID.matcher(raw).matches())
                ? raw
                : UUID.randomUUID().toString();

        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        return chain.filter(exchange)
                .contextWrite(Context.of(CORRELATION_ID_KEY, correlationId));
    }
}
