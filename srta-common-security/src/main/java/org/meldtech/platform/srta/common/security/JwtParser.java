package org.meldtech.platform.srta.common.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive component to extract claims from JWT tokens.
 */
@Component
public class JwtParser {

    public Mono<String> getSubject(Jwt jwt) {
        return Mono.justOrEmpty(jwt.getSubject());
    }

    public Mono<List<String>> getPermissions(Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions == null) {
            permissions = jwt.getClaimAsStringList("roles");
        }
        return Mono.justOrEmpty(permissions);
    }

    public Mono<Map<String, Object>> getClaims(Jwt jwt) {
        return Mono.just(jwt.getClaims());
    }
}
