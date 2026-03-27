package org.meldtech.platform.srta.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Hooks;

/**
 * Auto-configuration entry point for the srta-common-web module.
 * Registers shared web beans (exception handler, correlation filter, OpenAPI config).
 * All beans use {@code @ConditionalOnMissingBean} to allow consumer overrides.
 */
@AutoConfiguration
public class WebAutoConfiguration {

    @PostConstruct
    void enableContextPropagation() {
        Hooks.enableAutomaticContextPropagation();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalWebExceptionHandler globalWebExceptionHandler() {
        return new GlobalWebExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        return new GlobalErrorWebExceptionHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdWebFilter correlationIdWebFilter() {
        return new CorrelationIdWebFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenApiConfig openApiConfig() {
        return new OpenApiConfig();
    }
}
