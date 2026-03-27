package org.meldtech.platform.srta.common.audit;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuditEventTest {

    @Test
    void testAuditEventBuilder() {
        LocalDateTime now = LocalDateTime.now();
        AuditEvent event = AuditEvent.builder()
                .eventType("USER_LOGIN")
                .userId("user-123")
                .username("john_doe")
                .entityType("User")
                .entityId("user-123")
                .oldValue("{}")
                .newValue("{\"status\":\"ACTIVE\"}")
                .ipAddress("127.0.0.1")
                .correlationId("corr-123")
                .timestamp(now)
                .build();

        assertEquals("USER_LOGIN", event.getEventType());
        assertEquals("user-123", event.getUserId());
        assertEquals("john_doe", event.getUsername());
        assertEquals("User", event.getEntityType());
        assertEquals("user-123", event.getEntityId());
        assertEquals("{}", event.getOldValue());
        assertEquals("{\"status\":\"ACTIVE\"}", event.getNewValue());
        assertEquals("127.0.0.1", event.getIpAddress());
        assertEquals("corr-123", event.getCorrelationId());
        assertEquals(now, event.getTimestamp());
    }
}
