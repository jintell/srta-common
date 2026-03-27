package org.meldtech.platform.srta.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

class SecurityContextHelperTest {

    @Test
    void getCurrentUserId_returnsSubjectClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-42")
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

        StepVerifier.create(
                SecurityContextHelper.getCurrentUserId()
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
        ).expectNext("user-42").verifyComplete();
    }

    @Test
    void getCurrentJwt_returnsJwtToken() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-99")
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);

        StepVerifier.create(
                SecurityContextHelper.getCurrentJwt()
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
        ).expectNext(jwt).verifyComplete();
    }

    @Test
    void getAuthentication_returnsEmptyWhenNoContext() {
        StepVerifier.create(SecurityContextHelper.getAuthentication())
                .verifyComplete();
    }

    @Test
    void getCurrentUserId_returnsEmptyWhenNoContext() {
        StepVerifier.create(SecurityContextHelper.getCurrentUserId())
                .verifyComplete();
    }
}
