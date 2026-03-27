package org.meldtech.platform.srta.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Utility to interact with ReactiveSecurityContextHolder.
 */
public class SecurityContextHelper {

    public static Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

    public static Mono<Jwt> getCurrentJwt() {
        return getAuthentication()
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken);
    }

    public static Mono<String> getCurrentUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject);
    }
}
