package org.meldtech.platform.srta.common.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all entities with standard audit and versioning fields.
 * Uses Spring Data R2DBC annotations.
 */
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    private UUID id;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @Version
    private Long version;
}
