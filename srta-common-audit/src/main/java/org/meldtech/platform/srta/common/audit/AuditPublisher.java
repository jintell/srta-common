package org.meldtech.platform.srta.common.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

/**
 * Reactive RabbitMQ sender for audit logs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final Sender sender;
    private final ObjectMapper objectMapper;
    private static final String EXCHANGE_NAME = "srta.audit.exchange";
    private static final String ROUTING_KEY = "srta.audit.log";

    public Mono<Void> publish(AuditEvent event) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            OutboundMessage message = new OutboundMessage(EXCHANGE_NAME, ROUTING_KEY, body);
            return sender.send(Mono.just(message))
                    .doOnSuccess(v -> log.debug("Audit event published: {}", event.getEventType()))
                    .doOnError(e -> log.error("Failed to publish audit event: {}", event.getEventType(), e));
        } catch (JsonProcessingException e) {
            log.error("Error serializing audit event: {}", event.getEventType(), e);
            return Mono.error(e);
        }
    }
}
