package org.meldtech.platform.srta.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

/**
 * Reactive RabbitMQ sender for audit logs.
 */
@Slf4j
@RequiredArgsConstructor
public class AuditPublisher {

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE_NAME = "srta.audit.exchange";
    private static final String ROUTING_KEY = "srta.audit.log";

    public Mono<Void> publish(AuditEvent event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(event))
                .flatMap(body -> sender.send(Mono.just(new OutboundMessage(EXCHANGE_NAME, ROUTING_KEY, body))))
                .doOnSuccess(v -> log.debug("Audit event published: {}", event.getEventType()))
                .onErrorResume(e -> {
                    log.error("AUDIT PUBLISH FAILED — event={} error={}", event.getEventType(), e.getMessage());
                    return Mono.empty();
                });
    }
}
