package org.meldtech.platform.srta.common.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

/**
 * Custom converter to extract authorities from JWT claims.
 * Maps the "permissions" claim to Spring Security granted authorities.
 */
public class ReactiveJwtAuthenticationConverter extends ReactiveJwtAuthenticationConverterAdapter {

    public ReactiveJwtAuthenticationConverter() {
        super(buildDelegate());
    }

    private static JwtAuthenticationConverter buildDelegate() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("permissions");
        gac.setAuthorityPrefix("");
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(gac);
        return delegate;
    }
}
