package org.meldtech.platform.srta.common.audit;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * POJO representing an auditable action.
 */
@Value
@Builder
public class AuditEvent {
    String eventType;
    String userId;
    String username;
    String entityType;
    String entityId;
    String oldValue;
    String newValue;
    String ipAddress;
    String correlationId;
    LocalDateTime timestamp;
}
