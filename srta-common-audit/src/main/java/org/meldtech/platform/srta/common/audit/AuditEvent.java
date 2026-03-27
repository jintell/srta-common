package org.meldtech.platform.srta.common.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank String eventType;
    @NotBlank String userId;
    String username;
    String entityType;
    String entityId;
    String oldValue;
    String newValue;
    String ipAddress;
    String correlationId;
    @NotNull LocalDateTime timestamp;
}
