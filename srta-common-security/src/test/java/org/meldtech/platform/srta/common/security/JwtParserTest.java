package org.meldtech.platform.srta.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

class JwtParserTest {

    private final JwtParser jwtParser = new JwtParser();

    @Test
    void getSubject_returnsSubjectClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-1")
                .build();

        StepVerifier.create(jwtParser.getSubject(jwt))
                .expectNext("user-1")
                .verifyComplete();
    }

    @Test
    void getPermissions_returnsPermissionsClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("permissions", List.of("READ", "WRITE"))
                .build();

        StepVerifier.create(jwtParser.getPermissions(jwt))
                .expectNext(List.of("READ", "WRITE"))
                .verifyComplete();
    }

    @Test
    void getPermissions_fallsBackToRolesClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN"))
                .build();

        StepVerifier.create(jwtParser.getPermissions(jwt))
                .expectNext(List.of("ADMIN"))
                .verifyComplete();
    }

    @Test
    void getPermissions_returnsEmptyWhenNoClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-1")
                .build();

        StepVerifier.create(jwtParser.getPermissions(jwt))
                .verifyComplete();
    }

    @Test
    void getClaims_returnsAllClaims() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-1")
                .claim("custom", "value")
                .build();

        StepVerifier.create(jwtParser.getClaims(jwt))
                .expectNextMatches(claims -> "user-1".equals(claims.get("sub")) && "value".equals(claims.get("custom")))
                .verifyComplete();
    }
}
