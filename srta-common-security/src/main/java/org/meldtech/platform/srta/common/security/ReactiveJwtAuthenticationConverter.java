package org.meldtech.platform.srta.common.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

/**
 * Custom converter to extract authorities from JWT claims.
 */
public class ReactiveJwtAuthenticationConverter extends ReactiveJwtAuthenticationConverterAdapter {

    public ReactiveJwtAuthenticationConverter() {
        super(new JwtAuthenticationConverter());
    }
}
