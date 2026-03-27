package org.meldtech.platform.srta.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive component to extract claims from JWT tokens.
 */
@Slf4j
public class JwtParser {

    public Mono<String> getSubject(Jwt jwt) {
        return Mono.justOrEmpty(jwt.getSubject());
    }

    public Mono<List<String>> getPermissions(Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList("permissions");
        if (permissions != null) return Mono.just(permissions);
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            log.warn("JWT missing 'permissions' claim; falling back to 'roles' for subject={}", jwt.getSubject());
            return Mono.just(roles);
        }
        log.warn("JWT has neither 'permissions' nor 'roles' claim for subject={}", jwt.getSubject());
        return Mono.empty();
    }

    public Mono<Map<String, Object>> getClaims(Jwt jwt) {
        return Mono.just(jwt.getClaims());
    }
}
