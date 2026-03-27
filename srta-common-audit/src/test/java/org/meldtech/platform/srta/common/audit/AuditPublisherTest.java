package org.meldtech.platform.srta.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditPublisherTest {

    private Sender sender;
    private AuditPublisher publisher;

    @BeforeEach
    void setUp() {
        sender = mock(Sender.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        publisher = new AuditPublisher(sender, objectMapper);
    }

    @Test
    void publish_sendsMessageSuccessfully() {
        when(sender.send(any(Publisher.class))).thenReturn(Mono.empty());

        AuditEvent event = AuditEvent.builder()
                .eventType("USER_LOGIN")
                .userId("user-123")
                .username("john_doe")
                .timestamp(LocalDateTime.now())
                .build();

        StepVerifier.create(publisher.publish(event))
                .verifyComplete();

        verify(sender).send(any(Publisher.class));
    }

    @Test
    void publish_degradesGracefullyOnSenderFailure() {
        when(sender.send(any(Publisher.class))).thenReturn(Mono.error(new RuntimeException("RabbitMQ down")));

        AuditEvent event = AuditEvent.builder()
                .eventType("USER_LOGIN")
                .userId("user-123")
                .timestamp(LocalDateTime.now())
                .build();

        StepVerifier.create(publisher.publish(event))
                .verifyComplete();

        verify(sender).send(any(Publisher.class));
    }
}
