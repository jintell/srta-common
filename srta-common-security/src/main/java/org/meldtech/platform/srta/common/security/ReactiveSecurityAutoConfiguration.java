package org.meldtech.platform.srta.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Auto-configuration for reactive security defaults.
 * Provides a stateless JWT-based SecurityWebFilterChain if none is defined by the consumer.
 * All beans use {@code @ConditionalOnMissingBean} to allow consumer overrides.
 */
@AutoConfiguration
public class ReactiveSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    public SecurityWebFilterChain defaultSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeExchange(ex -> ex.anyExchange().authenticated())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtParser jwtParser() {
        return new JwtParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityContextHelper securityContextHelper() {
        return new SecurityContextHelper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        return new ReactiveJwtAuthenticationConverter();
    }
}
