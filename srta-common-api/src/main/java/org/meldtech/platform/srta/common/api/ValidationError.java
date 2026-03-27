package org.meldtech.platform.srta.common.api;

import lombok.Builder;
import lombok.Value;

/**
 * Details for field-level validation errors.
 */
@Value
@Builder
public class ValidationError {
    String field;
    String code;
    String message;
    Object rejectedValue;
}
