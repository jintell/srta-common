package org.meldtech.platform.srta.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationIdWebFilterTest {

    private final WebTestClient client = WebTestClient
            .bindToController(new TestController())
            .webFilter(new CorrelationIdWebFilter())
            .build();

    @Test
    void missingHeader_generatesUuidAndReturnsInResponse() {
        client.get().uri("/test")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(CorrelationIdWebFilter.CORRELATION_ID_HEADER)
                .expectHeader().value(CorrelationIdWebFilter.CORRELATION_ID_HEADER, value ->
                        assertTrue(value.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                                "Expected UUID format but got: " + value));
    }

    @Test
    void existingHeader_isForwardedToResponse() {
        String customId = "my-correlation-id-123";
        client.get().uri("/test")
                .header(CorrelationIdWebFilter.CORRELATION_ID_HEADER, customId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CorrelationIdWebFilter.CORRELATION_ID_HEADER, customId);
    }

    @Test
    void emptyHeader_generatesNewUuid() {
        client.get().uri("/test")
                .header(CorrelationIdWebFilter.CORRELATION_ID_HEADER, "")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value(CorrelationIdWebFilter.CORRELATION_ID_HEADER, value ->
                        assertTrue(value.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                                "Expected UUID format but got: " + value));
    }

    @RestController
    static class TestController {
        @GetMapping("/test")
        Mono<String> test() {
            return Mono.just("ok");
        }
    }
}
